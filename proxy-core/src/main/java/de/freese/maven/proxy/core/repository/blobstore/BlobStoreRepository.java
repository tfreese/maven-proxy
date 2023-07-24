// Created: 23.07.23
package de.freese.maven.proxy.core.repository.blobstore;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;

import de.freese.maven.proxy.blobstore.api.Blob;
import de.freese.maven.proxy.blobstore.api.BlobId;
import de.freese.maven.proxy.blobstore.api.BlobStore;
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
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod);
    }

    @Override
    protected boolean doExist(final URI resource) throws Exception {
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
    protected RepositoryResponse doGetInputStream(final URI resource) throws Exception {
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
    protected void doWrite(final URI resource, final InputStream inputStream) throws Exception {
        BlobId blobId = new BlobId(resource);

        getBlobStore().create(blobId, inputStream);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("written: {}", resource);
        }
    }

    protected BlobStore getBlobStore() {
        return blobStore;
    }
}
