// Created: 22.07.23
package de.freese.maven.proxy.main;

import java.io.File;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.maven.proxy.config.ProxyConfig;
import de.freese.maven.proxy.core.component.MavenProxyThreadFactory;
import de.freese.maven.proxy.core.component.ProxyUtils;
import de.freese.maven.proxy.core.lifecycle.LifecycleManager;
import de.freese.maven.proxy.core.repository.LocalRepository;
import de.freese.maven.proxy.core.repository.RemoteRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryManager;
import de.freese.maven.proxy.core.repository.VirtualRepository;
import de.freese.maven.proxy.core.repository.local.FileRepository;
import de.freese.maven.proxy.core.repository.remote.HttpRemoteRepository;
import de.freese.maven.proxy.core.repository.virtual.DefaultVirtualRepository;
import de.freese.maven.proxy.core.server.ProxyServer;
import de.freese.maven.proxy.core.server.jre.JreHttpServer;

/**
 * @author Thomas Freese
 */
public final class MavenProxyLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProxyLauncher.class);

    public static void main(String[] args) throws Exception {
        //        System.out.println(System.getProperty("user.dir"));

        Path configPath = findConfigFile(args);

        if (!Files.exists(configPath)) {
            LOGGER.error("maven-proxy config file not exist: {}", configPath);
            return;
        }

        LOGGER.info("Process User: {}", System.getProperty("user.name"));

        URL url = Thread.currentThread().getContextClassLoader().getSystemResource("xsd/proxy-config.xsd");
        Source schemaFile = new StreamSource(new File(url.toURI()));

        Source xmlFile = new StreamSource(configPath.toFile());

        // Validate Schema.
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");

        Schema schema = schemaFactory.newSchema(schemaFile);
        //        Validator validator = schema.newValidator();
        //        validator.validate(xmlFile);

        JAXBContext jaxbContext = JAXBContext.newInstance(ProxyConfig.class.getPackageName());
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        ProxyConfig proxyConfig = (ProxyConfig) unmarshaller.unmarshal(xmlFile);

        // ProxyUtils.setupProxy();

        //        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        //        root.setLevel(Level.INFO);

        int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 4);
        ExecutorService executorServiceHttpClient = new ThreadPoolExecutor(1, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MavenProxyThreadFactory("http-client-%d"));
        ExecutorService executorServiceHttpServer = new ThreadPoolExecutor(1, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MavenProxyThreadFactory("http-server-%d"));

        // @formatter:off
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofSeconds(3))
                .executor(executorServiceHttpClient)
                ;
        // @formatter:on
        // .authenticator(Authenticator.getDefault())
        // .cookieHandler(CookieHandler.getDefault())
        // .sslContext(SSLContext.getDefault())
        // .sslParameters(new SSLParameters())

        HttpClient httpClient = builder.build();

        LifecycleManager lifecycleManager = new LifecycleManager();

        ProxyServer proxyServer = new JreHttpServer().setPort(proxyConfig.getPort()).setExecutor(executorServiceHttpServer);

        RepositoryManager repositoryManager = new RepositoryManager();

        // LocalRepository
        proxyConfig.getRepositories().getLocals().stream().forEach(localRepo -> {
            URI uri = URI.create(localRepo.getPath());

            if (uri.getScheme().equalsIgnoreCase("file")) {
                LocalRepository localRepository = new FileRepository(localRepo.getName(), Paths.get(uri));
                lifecycleManager.add(localRepository);
                repositoryManager.add(localRepository);
                proxyServer.addContextRoot(localRepo.getName(), localRepository);
            }
            else {
                LOGGER.error("Ignoring LocalRepository '{}', file URI scheme expected: {}", localRepo.getName(), uri);
            }
        });

        // RemoteRepository
        proxyConfig.getRepositories().getRemotes().stream().forEach(remoteRepo -> {
            URI uri = URI.create(remoteRepo.getUrl());

            if (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                RemoteRepository remoteRepository = new HttpRemoteRepository(remoteRepo.getName(), uri, httpClient);
                lifecycleManager.add(remoteRepository);
                repositoryManager.add(remoteRepository);
                proxyServer.addContextRoot(remoteRepository.getName(), remoteRepository);
            }
            else {
                LOGGER.error("Ignoring RemoteRepository '{}', http/https URI scheme expected: {}", remoteRepo.getName(), uri);
            }
        });

        // VirtualRepository
        proxyConfig.getRepositories().getVirtuals().stream().forEach(virtualRepo -> {
            if (!virtualRepo.getRepositoryNames().isEmpty()) {
                VirtualRepository virtualRepository = new DefaultVirtualRepository(virtualRepo.getName());

                for (String repositoryName : virtualRepo.getRepositoryNames()) {
                    Repository repository = repositoryManager.getRepository(repositoryName);

                    if (repository == null) {
                        LOGGER.error("Repository not found or configured: ", repositoryName);
                        continue;
                    }

                    if (repository instanceof LocalRepository lr) {
                        virtualRepository.add(lr);
                    }
                    else if (repository instanceof RemoteRepository rr) {
                        virtualRepository.add(rr);
                    }
                    else {
                        LOGGER.error("VirtualRepository can only contain LocalRepository or RemoteRepository: ", repository.getClass().getSimpleName());
                    }
                }

                lifecycleManager.add(virtualRepository);
                proxyServer.addContextRoot(virtualRepository.getName(), virtualRepository);
            }
            else {
                LOGGER.error("Ignoring VirtualRepository '{}', no repositories configured", virtualRepo.getName());
            }
        });

        // Server at last
        lifecycleManager.add(proxyServer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lifecycleManager.stop();
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            ProxyUtils.shutdown(executorServiceHttpServer, LOGGER);
            ProxyUtils.shutdown(executorServiceHttpClient, LOGGER);
        }, "Shutdown"));

        try {
            lifecycleManager.start();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);

            System.exit(-1);
        }
    }

    private static Path findConfigFile(final String[] args) {
        if (args != null && args.length == 2) {
            String parameter = args[0];

            if ("-maven-proxy.config".equals(parameter)) {
                return Paths.get(args[1]);
            }
        }
        else if (System.getProperty("maven-proxy.config") != null) {
            return Paths.get(System.getProperty("maven-proxy.config"));
        }
        else if (System.getenv("maven-proxy.config") != null) {
            return Paths.get(System.getenv("maven-proxy.config"));
        }

        LOGGER.error("no maven-proxy config file found");
        LOGGER.error("define it as programm argument: -maven-proxy.config <ABSOLUTE_PATH>/proxy-config.xml");
        LOGGER.error("or as system property: -Dmaven-proxy.config=<ABSOLUTE_PATH>/proxy-config.xml");
        LOGGER.error("or as environment variable: set/export maven-proxy.config=<ABSOLUTE_PATH>/proxy-config.xml");

        throw new IllegalStateException("no maven-proxy config file found");
    }

    private MavenProxyLauncher() {
        super();
    }
}
