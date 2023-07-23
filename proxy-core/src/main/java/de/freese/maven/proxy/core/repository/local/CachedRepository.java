// Created: 03.05.2021
package de.freese.maven.proxy.core.repository.local;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.component.ProxyUtils;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.LocalRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class CachedRepository extends AbstractRepository implements LocalRepository {

    private final Repository delegate;

    private final Path path;

    public CachedRepository(final Repository delegate, final Path path) throws Exception {
        super(delegate.getName() + "-cached");

        this.delegate = Objects.requireNonNull(delegate, "delegate required");
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

        if (Files.exists(path)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("exist - found: {}", path);
            }

            return true;
        }

        return this.delegate.exist(resource);
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        if (resource.getPath().endsWith("maven-metadata.xml")) {
            // Never save these files, versions:display-dependency-updates won't work !
            return this.delegate.getInputStream(resource);
        }

        Path path = toPath(resource);

        if (Files.exists(path)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - use cached: {}", path);
            }

            return new RepositoryResponse(resource, Files.size(path), Files.newInputStream(path));
        }
        else {
            Files.createDirectories(path.getParent());

            RepositoryResponse response = this.delegate.getInputStream(resource);

            if (response != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Download {}, {} Bytes = {}", response.getUri(), response.getContentLength(), ProxyUtils.toHumanReadable(response.getContentLength()));
                }

                return new CachedRepositoryResponse(response, path);
            }
        }

        return null;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod);
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    @Override
    public void write(final URI uri, final InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("read only repository: " + getName());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!Files.exists(getPath())) {
            Files.createDirectories(getPath());
        }

        if (!Files.isReadable(getPath())) {
            throw new IllegalStateException("path not readable: " + getPath());
        }

        if (!Files.isWritable(getPath())) {
            throw new IllegalStateException("path not writeable: " + getPath());
        }
    }

    private Path toPath(final URI resource) {
        Path relativePath = toRelativePath(resource);

        return getPath().resolve(relativePath);
    }
}
