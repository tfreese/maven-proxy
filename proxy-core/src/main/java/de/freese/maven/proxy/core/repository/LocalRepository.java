// Created: 19.07.23
package de.freese.maven.proxy.core.repository;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author Thomas Freese
 */
public interface LocalRepository extends Repository {

    Path getPath();

    void write(URI uri, InputStream inputStream) throws Exception;
}
