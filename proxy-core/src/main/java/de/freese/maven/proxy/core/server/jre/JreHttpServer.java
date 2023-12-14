// Created: 22.07.23
package de.freese.maven.proxy.core.server.jre;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import de.freese.maven.proxy.core.server.AbstractProxyServer;
import de.freese.maven.proxy.core.utils.MavenProxyThreadFactory;
import de.freese.maven.proxy.core.utils.ProxyUtils;

/**
 * @author Thomas Freese
 */
public class JreHttpServer extends AbstractProxyServer {

    private final List<HttpContext> httpContexts = new ArrayList<>();

    private ExecutorService executorService;
    private HttpServer httpServer;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        final int port = getServerConfig().getPort();
        final int threadPoolCoreSize = getServerConfig().getThreadPoolCoreSize();
        final int threadPoolMaxSize = getServerConfig().getThreadPoolMaxSize();
        final String threadNamePattern = getServerConfig().getThreadNamePattern();

        this.executorService = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MavenProxyThreadFactory(threadNamePattern));

        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.httpServer.setExecutor(executorService);

        getContextRoots().forEach((contextRoot, repository) -> {
            final String path = contextRoot.startsWith("/") ? contextRoot : "/" + contextRoot;

            getLogger().info("add contextRoot '{}' for {}", path, repository.getClass().getSimpleName());

            final HttpContext httpContext = this.httpServer.createContext(path, new JreHttpServerHandler(repository));
            httpContexts.add(httpContext);
        });

        this.httpServer.start();
        //        new Thread(this.httpServer::start, "Maven-Proxy").start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        this.httpContexts.clear();
        this.httpServer.stop(3);

        ProxyUtils.shutdown(this.executorService, getLogger());
    }
}
