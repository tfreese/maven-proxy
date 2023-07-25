// Created: 02.05.2021
package de.freese.maven.proxy.core.repository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

/**
 * @author Thomas Freese
 */
public class RepositoryResponse {

    private final long contentLength;

    private final InputStream inputStream;

    private final URI uri;

    public RepositoryResponse(final URI uri, final long contentLength, final InputStream inputStream) {
        super();

        this.uri = Objects.requireNonNull(uri, "uri required");
        this.contentLength = contentLength;
        this.inputStream = Objects.requireNonNull(inputStream, "inputStream required");
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public String getFileName() {
        String path = this.uri.toString();
        int lastSlashIndex = path.lastIndexOf('/');

        return path.substring(lastSlashIndex + 1);
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public URI getUri() {
        return this.uri;
    }

    public long transferTo(final OutputStream outputStream) throws IOException {
        try (InputStream is = new BufferedInputStream(getInputStream())) {
            return is.transferTo(outputStream);
        }
    }
}
