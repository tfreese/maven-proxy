// Created: 22.07.23
package de.freese.maven.proxy.core.repository;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;

/**
 * @author Thomas Freese
 */
public abstract class AbstractRepository extends AbstractLifecycle implements Repository {

    private final String name;

    protected AbstractRepository(final String name) {
        super();

        this.name = Objects.requireNonNull(name, "name required");
    }

    @Override
    public String getName() {
        return name;
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
