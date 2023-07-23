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
        URI uri = URI.create("/public/org/maven/pom.xml");

        System.out.println(uri.relativize(URI.create("/public")));
        System.out.println(URI.create("/public").relativize(uri));
        System.out.println(URI.create(uri.getPath().replace("/public", "")));
        System.out.println(uri.resolve("/public"));
        System.out.println(uri.normalize());
    }

    private Misc() {
        super();
    }
}
