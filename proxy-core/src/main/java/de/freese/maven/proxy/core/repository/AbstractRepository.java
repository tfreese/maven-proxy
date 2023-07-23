// Created: 22.07.23
package de.freese.maven.proxy.core.repository;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;

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
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return getName() + ": " + getUri();
    }

    protected Path toRelativePath(final URI resource) {
        String uriPath = resource.getPath();
        uriPath = uriPath.replace(' ', '_');

        if (uriPath.startsWith("/")) {
            uriPath = uriPath.substring(1);
        }

        return Paths.get(uriPath);
    }
}
