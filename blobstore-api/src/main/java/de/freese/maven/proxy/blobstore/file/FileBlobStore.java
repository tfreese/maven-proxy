// Created: 18.09.2019
package de.freese.maven.proxy.blobstore.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import de.freese.maven.proxy.blobstore.api.AbstractBlobStore;
import de.freese.maven.proxy.blobstore.api.Blob;
import de.freese.maven.proxy.blobstore.api.BlobId;

/**
 * @author Thomas Freese
 */
public class FileBlobStore extends AbstractBlobStore {

    private final URI uri;

    public FileBlobStore(final URI uri) {
        super();

        this.uri = Objects.requireNonNull(uri, "URI required");
    }

    @Override
    public OutputStream create(final BlobId id) throws Exception {
        Path path = toContentPath(id);

        Files.createDirectories(path.getParent());

        return Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void create(final BlobId id, final InputStream inputStream) throws Exception {
        Path path = toContentPath(id);

        Files.createDirectories(path.getParent());

        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void delete(final BlobId id) throws Exception {
        Path path = toContentPath(id);

        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Override
    public boolean exists(final BlobId id) throws Exception {
        Path path = toContentPath(id);

        return Files.exists(path);
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    Path toContentPath(final BlobId id) {
        URI uri = id.getUri();
        String uriString = uri.getPath();

        uriString = uriString.replace(':', '/');
        uriString = uriString.replace('?', '/');
        uriString = uriString.replace('&', '/');
        uriString = uriString.replace(' ', '_');
        uriString = uriString.replace("%20", "_");

        while (uriString.contains("//")) {
            uriString = uriString.replace("//", "/");
        }

        if (uriString.startsWith("/")) {
            uriString = uriString.substring(1);
        }

        return Paths.get(getUri()).resolve(uriString);

        //        byte[] uriBytes = uriString.getBytes(StandardCharsets.UTF_8);
        //        byte[] digest = getMessageDigest().digest(uriBytes);
        //        String hex = HexFormat.of().withUpperCase().formatHex(uriBytes);
        //
        //        Path path = this.basePath;
        //
        //        // Build Structure in the Cache-Directory.
        //        for (int i = 0; i < 3; i++)
        //        {
        //            path = path.resolve(hex.substring(i * 2, (i * 2) + 2));
        //        }
        //
        //        return this.basePath.resolve(hex);
    }

    @Override
    protected Blob doGet(final BlobId id) throws Exception {
        return new FileBlob(id, toContentPath(id));
    }
}
