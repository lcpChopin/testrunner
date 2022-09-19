package edu.illinois.cs.testrunner.agent;

import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Helper {
    private final static Map<String, String> tmp = new ConcurrentHashMap<>();;

    public static void store(String str) {
        tmp.put(str, str);
    }

    public static void dummy() {
        tmp.put("dummy", "dummy");
    }

    public void print(String test) {
        for (String item : tmp.keySet()) {
            System.out.println("PAIR: " + test + "," + item);
        }
    }

    public void clear() {
        tmp.clear();
    }


}
