// Created: 22.07.23
package de.freese.maven.proxy.core.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;
import de.freese.maven.proxy.core.repository.Repository;

/**
 * @author Thomas Freese
 */
public abstract class AbstractProxyServer extends AbstractLifecycle implements ProxyServer {

    private final Map<String, Repository> contextRoots = new LinkedHashMap<>();

    private Executor executor;

    private int port = -1;

    @Override
    public ProxyServer addContextRoot(final String contextRoot, final Repository repository) {
        checkNotNull(contextRoot, "ContextRoot");
        checkNotNull(repository, "Repository");

        if (contextRoots.containsKey(repository.getName())) {
            throw new IllegalArgumentException("ContextRoot already exist: " + repository.getName());
        }

        contextRoots.put(contextRoot, repository);

        return this;
    }

    @Override
    public ProxyServer setExecutor(final Executor executor) {
        this.executor = executor;

        return this;
    }

    @Override
    public ProxyServer setPort(final int port) {
        this.port = port;

        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("port=").append(port);
        sb.append(']');

        return sb.toString();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        checkNotNull(executor, "Executor");
        checkValue(port, value -> value <= 0 ? "Port has invalid range: " + value : null);
        checkValue(contextRoots, value -> value.isEmpty() ? "ContextRoots are not defined" : null);
    }

    protected Map<String, Repository> getContextRoots() {
        return Map.copyOf(contextRoots);
    }

    protected Executor getExecutor() {
        return executor;
    }

    protected int getPort() {
        return port;
    }
}
