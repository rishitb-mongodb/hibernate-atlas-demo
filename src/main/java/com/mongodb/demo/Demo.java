package com.mongodb.demo;

import com.mongodb.demo.acts.Act1;
import com.mongodb.demo.acts.Act2;
import com.mongodb.demo.acts.Act3;
import com.mongodb.demo.acts.ResetData;
import com.mongodb.demo.acts.Verify;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

/**
 * Entry point. Each act runs on its own.
 *
 * <pre>
 *   mvn -q compile exec:java -Dexec.args=verify
 *   mvn -q compile exec:java -Dexec.args=1
 *   mvn -q compile exec:java -Dexec.args=2
 *   mvn -q compile exec:java -Dexec.args=3
 *   mvn -q compile exec:java -Dexec.args=reset-data
 * </pre>
 */
public final class Demo {

    private Demo() {}

    public static void main(String[] args) {
        // Must happen before Hibernate bootstraps and before logback binds System.out.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        var act = args.length == 0 ? "verify" : args[0].trim().toLowerCase();
        var before = Mql.printCount();

        switch (act) {
            case "verify" -> Verify.run();
            case "1" -> Act1.run();
            case "2" -> Act2.run();
            case "3" -> Act3.run();
            case "reset-data" -> ResetData.run();
            default -> {
                System.err.println("Unknown act: " + act);
                System.err.println("Use one of: verify, 1, 2, 3, reset-data");
                System.exit(2);
            }
        }

        if (Mql.printCount() == before && "2".equals(act)) {
            System.out.println();
            System.out.println("Act 2 printed nothing -- its code is still commented out.");
            System.out.println("Reveal it with:  ./reveal show     (./reveal.ps1 show on Windows)");
        }
        System.out.println();
    }
}
