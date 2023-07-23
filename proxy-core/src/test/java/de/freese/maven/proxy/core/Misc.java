// Created: 23.07.23
package de.freese.maven.proxy.core;

import java.net.URI;

/**
 * @author Thomas Freese
 */
public final class Misc {
    public static void main(String[] args) throws Exception {
        testUrl();
    }

    private static void testUrl() throws Exception {
        URI uri = URI.create("file:///tmp/maven-proxy/cache/");

        String relative = "public-cached";

        System.out.println(uri.relativize(URI.create(relative)));
        System.out.println(uri.resolve(URI.create(relative)));
        System.out.println(uri.resolve(relative));
    }

    private Misc() {
        super();
    }
}
