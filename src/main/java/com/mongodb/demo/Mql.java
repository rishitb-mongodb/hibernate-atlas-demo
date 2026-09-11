package com.mongodb.demo;

import org.bson.BsonDocument;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

/** Console helpers. Nothing to do with the extension -- they just keep the demo output readable. */
public final class Mql {

    private static final JsonWriterSettings PRETTY =
            JsonWriterSettings.builder().outputMode(JsonMode.SHELL).indent(true).build();

    private static int printCount;

    private Mql() {}

    /** Pretty-prints an aggregation pipeline so it is legible on a projector. */
    public static String pretty(String mql) {
        try {
            return BsonDocument.parse(mql).toJson(PRETTY);
        } catch (RuntimeException e) {
            return mql;
        }
    }

    public static void heading(String text) {
        printCount++;
        System.out.println();
        System.out.println(">>> " + text);
    }

    public static void note(String text) {
        printCount++;
        System.out.println("    # " + text);
    }

    public static void row(Object value) {
        printCount++;
        System.out.println("    - " + value);
    }

    /** Used only so that {@link Demo} can tell whether an act is still commented out. */
    public static int printCount() {
        return printCount;
    }
}
