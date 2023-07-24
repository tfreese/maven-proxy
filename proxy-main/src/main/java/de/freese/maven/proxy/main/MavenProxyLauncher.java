// Created: 22.07.23
package de.freese.maven.proxy.main;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
import de.freese.maven.proxy.config.RepositoryBuilder;
import de.freese.maven.proxy.core.component.JreHttpClientComponent;
import de.freese.maven.proxy.core.lifecycle.LifecycleManager;
import de.freese.maven.proxy.core.repository.RepositoryManager;
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

        LifecycleManager lifecycleManager = new LifecycleManager();

        JreHttpClientComponent httpClientComponent = new JreHttpClientComponent(proxyConfig.getClientConfig());
        lifecycleManager.add(httpClientComponent);

        ProxyServer proxyServer = new JreHttpServer().setConfig(proxyConfig.getServerConfig());

        RepositoryManager repositoryManager = new RepositoryManager();

        // @formatter:off
        // LocalRepository
        proxyConfig.getRepositories().getLocals().stream()
                .map(localRepoConfig -> RepositoryBuilder.buildLocal(localRepoConfig, lifecycleManager, repositoryManager))
                .filter(Objects::nonNull)
                .forEach(repository -> proxyServer.addContextRoot(repository.getName(), repository))
        ;

        // RemoteRepository
        proxyConfig.getRepositories().getRemotes().stream()
                .map(remoteRepoConfig -> RepositoryBuilder.buildRemote(remoteRepoConfig, lifecycleManager, repositoryManager, httpClientComponent))
                .filter(Objects::nonNull)
                .forEach(repository -> proxyServer.addContextRoot(repository.getName(), repository))
        ;

        // VirtualRepository
        proxyConfig.getRepositories().getVirtuals().stream()
                .map(virtualRepoConfig -> RepositoryBuilder.buildVirtual(virtualRepoConfig, lifecycleManager, repositoryManager))
                .filter(Objects::nonNull)
                .forEach(repository -> proxyServer.addContextRoot(repository.getName(), repository))
        ;
        // @formatter:on

        // Server at last
        lifecycleManager.add(proxyServer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lifecycleManager.stop();
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
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
