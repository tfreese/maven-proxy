// Created: 18.09.2019
package de.freese.maven.proxy.blobstore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public abstract class AbstractBlobStore implements BlobStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final Blob get(final BlobId id) throws Exception {
        return doGet(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("uri=").append(getUri());
        sb.append(']');

        return sb.toString();
    }

    protected abstract Blob doGet(BlobId id) throws Exception;

    protected Logger getLogger() {
        return this.logger;
    }
}
