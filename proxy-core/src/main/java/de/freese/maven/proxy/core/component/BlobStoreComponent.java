// Created: 24.07.23
package de.freese.maven.proxy.core.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.freese.maven.proxy.blobstore.api.BlobStore;
import de.freese.maven.proxy.blobstore.file.FileBlobStore;
import de.freese.maven.proxy.blobstore.jdbc.JdbcBlobStore;
import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;

/**
 * @author Thomas Freese
 */
public class BlobStoreComponent extends AbstractLifecycle {

    private final BlobStore blobStore;

    public BlobStoreComponent(final BlobStore blobStore) {
        super();

        this.blobStore = checkNotNull(blobStore, "BlobStore");
    }

    public BlobStore getBlobStore() {
        return blobStore;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("blobStore=").append(blobStore);
        sb.append(']');

        return sb.toString();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (blobStore instanceof FileBlobStore) {
            Path path = Paths.get(blobStore.getUri());

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
        else if (blobStore instanceof JdbcBlobStore jdbcBlobStore) {
            jdbcBlobStore.createDatabaseIfNotExist();
        }
    }
}
