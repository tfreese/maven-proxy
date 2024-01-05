// Created: 03.05.2021
package de.freese.maven.proxy.core.repository.cached;

import java.net.URI;

import de.freese.maven.proxy.blobstore.api.Blob;
import de.freese.maven.proxy.blobstore.api.BlobId;
import de.freese.maven.proxy.blobstore.api.BlobStore;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;
import de.freese.maven.proxy.core.utils.HttpMethod;
import de.freese.maven.proxy.core.utils.ProxyUtils;

/**
 * @author Thomas Freese
 */
public class CachedRepository extends AbstractRepository {

    private final BlobStore blobStore;
    private final Repository delegate;

    public CachedRepository(final Repository delegate, final BlobStore blobStore) {
        super(delegate.getName(), delegate.getUri());

        this.delegate = checkNotNull(delegate, "Repository");
        this.blobStore = checkNotNull(blobStore, "BlobStore");
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    protected boolean doExist(final URI resource) throws Exception {
        final BlobId blobId = new BlobId(resource);

        final boolean exist = getBlobStore().exists(blobId);

        if (exist) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("exist - found: {}", resource);
            }

            return true;
        }
        else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("exist - not found: {}", resource);
            }

            return this.delegate.exist(resource);
        }
    }

    @Override
    protected RepositoryResponse doGetInputStream(final URI resource) throws Exception {
        if (resource.getPath().endsWith("maven-metadata.xml")) {
            // Never save these files, versions:display-dependency-updates won't work !
            return this.delegate.getInputStream(resource);
        }

        final BlobId blobId = new BlobId(resource);

        if (getBlobStore().exists(blobId)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - found: {}", resource);
            }

            final Blob blob = getBlobStore().get(blobId);

            return new RepositoryResponse(resource, blob.getLength(), blob.getInputStream());
        }
        else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - not found: {}", resource);
            }

            final RepositoryResponse response = this.delegate.getInputStream(resource);

            if (response != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Download {} Bytes [{}]: {} ", response.getContentLength(), ProxyUtils.toHumanReadable(response.getContentLength()), response.getUri());
                }

                return new CachedRepositoryResponse(response, blobId, getBlobStore());
            }
        }

        return null;
    }

    protected BlobStore getBlobStore() {
        return blobStore;
    }
}
