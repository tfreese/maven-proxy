// Created: 18.09.2019
package de.freese.maven.proxy.blobstore.api;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

/**
 * Unique ID for a Blob from a {@link BlobStore}.<br>
 * <a href="https://github.com/sonatype/nexus-public/blob/master/components/nexus-blobstore-api">nexus-blobstore-api</a><br>
 * <a href="https://github.com/sonatype/goodies/tree/main/lifecycle">goodies</a>
 *
 * @author Thomas Freese
 */
public class BlobId implements Serializable, Comparable<BlobId> {
    @Serial
    private static final long serialVersionUID = -5581749917166864024L;

    private final URI uri;

    public BlobId(final URI uri) {
        super();

        this.uri = Objects.requireNonNull(uri, "uri required");
    }

    @Override
    public int compareTo(final BlobId o) {
        return this.uri.compareTo(o.uri);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final BlobId blobId = (BlobId) o;

        return this.uri.equals(blobId.uri);
    }

    public URI getUri() {
        return this.uri;
    }

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    @Override
    public String toString() {
        return this.uri.toString();
    }
}
