// Created: 23.07.23
package de.freese.maven.proxy.core;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thomas Freese
 */
public final class Misc {
    public static void main(final String[] args) throws Exception {
        //        testUrl();
        removeSnapshotTimestamp();
    }

    private static void removeSnapshotTimestamp() throws Exception {
        Pattern pattern = Pattern.compile("\\d{8}\\.\\d{6}-\\d");
        Matcher matcher = pattern.matcher("/de/freese/maven/proxy/test-project/0.0.1-SNAPSHOT/test-project-0.0.1-20230806.084242-1.pom");

        if (matcher.find()) {
            System.out.println(matcher.group());
        }

        System.out.println(matcher.replaceAll("SNAPSHOT"));
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
