// Created: 22.07.23
package de.freese.maven.proxy.core.repository.local;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;
import de.freese.maven.proxy.core.utils.HttpMethod;

/**
 * @author Thomas Freese
 */
public class FileRepository extends AbstractRepository {

    public FileRepository(final String name, final URI uri) {
        super(name, uri);
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    protected boolean doExist(final URI resource) throws Exception {
        final Path path = toPath(resource);

        final boolean exist = Files.exists(path);

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
    protected RepositoryResponse doGetInputStream(final URI resource) throws Exception {
        final Path path = toPath(resource);

        if (Files.exists(path)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - found: {}", path);
            }

            return new RepositoryResponse(resource, Files.size(path), Files.newInputStream(path));
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream - not found: {}", path);
        }

        return null;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        final Path path = Paths.get(getUri());

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalStateException("path not readable: " + path);
        }
    }

    protected Path toPath(final URI resource) {
        final Path relativePath = toRelativePath(resource);

        return Paths.get(getUri()).resolve(relativePath);
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
