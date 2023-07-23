// Created: 03.05.2021
package de.freese.maven.proxy.core.repository.local;

import java.io.BufferedInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.blobstore.api.Blob;
import de.freese.maven.proxy.blobstore.api.BlobId;
import de.freese.maven.proxy.blobstore.api.BlobStore;
import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.component.ProxyUtils;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class CachedRepository extends AbstractLocalRepository {

    private final BlobStore blobStore;

    private final Repository delegate;

    public CachedRepository(final Repository delegate, final URI uri, final BlobStore blobStore) {
        super(delegate.getName(), uri);

        this.delegate = checkNotNull(delegate, "Repository");
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
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        if (resource.getPath().endsWith("maven-metadata.xml")) {
            // Never save these files, versions:display-dependency-updates won't work !
            return this.delegate.getInputStream(resource);
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
        else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("getInputStream - not found: {}", resource);
            }

            RepositoryResponse response = this.delegate.getInputStream(resource);

            if (response != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Download {} Bytes [{}]: {} ", response.getContentLength(), ProxyUtils.toHumanReadable(response.getContentLength()), response.getUri());
                }

                return new CachedRepositoryResponse(response, blobId, getBlobStore());
            }
        }

        return null;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        Path path = Paths.get(getUri());

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
