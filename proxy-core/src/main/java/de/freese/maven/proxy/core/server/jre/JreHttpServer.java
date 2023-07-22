// Created: 22.07.23
package de.freese.maven.proxy.core.server.jre;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import de.freese.maven.proxy.core.server.AbstractProxyServer;

/**
 * @author Thomas Freese
 */
public class JreHttpServer extends AbstractProxyServer {

    private final List<HttpContext> httpContexts = new ArrayList<>();

    private HttpServer httpServer;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        this.httpServer = HttpServer.create(new InetSocketAddress(getPort()), 0);
        this.httpServer.setExecutor(getExecutor());

        getContextRoots().forEach((contextRoot, repository) -> {
            String path = "/" + contextRoot;

            getLogger().info("{}: add contextRoot {}", repository.getClass().getSimpleName(), path);

            HttpContext httpContext = this.httpServer.createContext(path, new JreHttpServerHandler(repository));
            httpContexts.add(httpContext);
        });

        new Thread(this.httpServer::start, "Maven-Proxy").start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        this.httpServer.stop(3);
    }
}
