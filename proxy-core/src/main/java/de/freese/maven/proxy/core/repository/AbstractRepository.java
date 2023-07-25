// Created: 22.07.23
package de.freese.maven.proxy.core.repository;

import java.io.InputStream;
import java.net.URI;

import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;
import de.freese.maven.proxy.core.utils.HttpMethod;

/**
 * @author Thomas Freese
 */
public abstract class AbstractRepository extends AbstractLifecycle implements Repository {

    private final String name;

    private final URI uri;

    protected AbstractRepository(final String name, final URI uri) {
        super();

        this.name = checkNotNull(name, "Name");
        this.uri = checkNotNull(uri, "URI");
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            getLogger().warn("Component not started: {}" + getName());
            return false;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("exist: {}", resource);
        }

        return doExist(resource);
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            getLogger().warn("Component not started: {}" + getName());
            return null;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream: {}", resource);
        }

        return doGetInputStream(resource);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return false;
    }

    @Override
    public String toString() {
        return getName() + ": " + getUri();
    }

    @Override
    public void write(final URI resource, final InputStream inputStream) throws Exception {
        if (!isStarted()) {
            getLogger().warn("Component not started: {}" + getName());
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("write: {}", resource);
        }

        doWrite(resource, inputStream);
    }

    protected abstract boolean doExist(URI resource) throws Exception;

    protected abstract RepositoryResponse doGetInputStream(URI resource) throws Exception;

    protected void doWrite(final URI resource, final InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("read only repository: " + getName() + " - " + getUri());
    }
}
