// Created: 23.07.23
package de.freese.maven.proxy.core.repository.blobstore;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.blobstore.api.Blob;
import de.freese.maven.proxy.blobstore.api.BlobId;
import de.freese.maven.proxy.blobstore.api.BlobStore;
import de.freese.maven.proxy.blobstore.jdbc.JdbcBlobStore;
import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class BlobStoreRepository extends AbstractRepository {

    private final BlobStore blobStore;

    public BlobStoreRepository(final String name, final URI uri, final BlobStore blobStore) {
        super(name, uri);

        this.blobStore = checkNotNull(blobStore, "BlobStore");
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            return false;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("exist: {}", resource);
        }

        BlobId blobId = new BlobId(resource);

        boolean exist = getBlobStore().exists(blobId);

        if (getLogger().isDebugEnabled()) {
            if (exist) {
                getLogger().debug("exist - found: {}", resource);
            }
            else {
                getLogger().debug("exist - not found: {}", resource);
            }
        }

        return exist;
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream: {}", resource);
        }

        BlobId blobId = new BlobId(resource);

        if (getBlobStore().exists(blobId)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - found: {}", resource);
            }

            Blob blob = getBlobStore().get(blobId);

            return new RepositoryResponse(resource, blob.getLength(), new BufferedInputStream(blob.getInputStream()));
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream - not found: {}", resource);
        }

        return null;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod);
    }

    @Override
    public void write(final URI resource, final InputStream inputStream) throws Exception {
        if (!isStarted()) {
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("write: {}", resource);
        }

        BlobId blobId = new BlobId(resource);

        getBlobStore().create(blobId, inputStream);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("written: {}", resource);
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (getBlobStore() instanceof JdbcBlobStore jdbcBlobStore) {
            jdbcBlobStore.createDatabaseIfNotExist();
        }

        Path path = Paths.get(getUri());

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalStateException("path not readable: " + path);
        }

        if (!Files.isWritable(path)) {
            throw new IllegalStateException("path not writeable: " + path);
        }
    }

    protected BlobStore getBlobStore() {
        return blobStore;
    }
}
