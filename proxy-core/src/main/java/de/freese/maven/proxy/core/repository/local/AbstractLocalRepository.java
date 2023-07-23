// Created: 23.07.23
package de.freese.maven.proxy.core.repository.local;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.core.repository.AbstractRepository;

/**
 * @author Thomas Freese
 */
public abstract class AbstractLocalRepository extends AbstractRepository {

    protected AbstractLocalRepository(final String name, final URI uri) {
        super(name, uri);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        Path path = Paths.get(getUri());

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    protected Path toPath(final URI resource) {
        Path relativePath = toRelativePath(resource);

        return Paths.get(getUri()).resolve(relativePath);
    }
}
