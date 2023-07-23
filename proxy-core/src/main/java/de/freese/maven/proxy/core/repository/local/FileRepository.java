// Created: 22.07.23
package de.freese.maven.proxy.core.repository.local;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.LocalRepository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class FileRepository extends AbstractRepository implements LocalRepository {

    private final Path path;

    public FileRepository(final String name, final Path path) {
        super(name);

        this.path = Objects.requireNonNull(path, "path required");
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            return false;
        }

        Path path = toPath(resource);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("exist: {}", path);
        }

        boolean exist = Files.exists(path);

        if (getLogger().isDebugEnabled()) {
            if (exist) {
                getLogger().debug("exist - found: {}", path);
            }
            else {
                getLogger().debug("exist - not found: {}", path);
            }
        }

        return exist;
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        Path path = toPath(resource);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream: {}", path);
        }

        if (Files.exists(path)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - found: {}", path);
            }

            return new RepositoryResponse(resource, Files.size(path), new BufferedInputStream(Files.newInputStream(path)));
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream - not found: {}", path);
        }

        return null;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    public String toString() {
        return getName() + ": " + getPath().toString();
    }

    @Override
    public void write(final URI uri, final InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("read only repository: " + getName());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!Files.isReadable(getPath())) {
            throw new IllegalStateException("path not readable: " + getPath());
        }
    }

    protected Path toPath(final URI resource) {
        Path relativePath = toRelativePath(resource);

        return getPath().resolve(relativePath);
    }
}
