// Created: 22.07.23
package de.freese.maven.proxy.core.repository.local;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.freese.maven.proxy.core.component.HttpMethod;

/**
 * @author Thomas Freese
 */
public class WriteableFileRepository extends FileRepository {

    public WriteableFileRepository(final String name, final Path path) {
        super(name, path);
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return super.supports(httpMethod) || HttpMethod.PUT.equals(httpMethod);
    }

    @Override
    public void write(final URI uri, final InputStream inputStream) throws Exception {
        if (!isStarted()) {
            return;
        }

        Path path = toPath(uri);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("write: {}", path);
        }

        Files.createDirectories(path.getParent());

        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            inputStream.transferTo(outputStream);

            outputStream.flush();
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!Files.isWritable(getPath())) {
            throw new IllegalStateException("path not writeable: " + getPath());
        }
    }
}
