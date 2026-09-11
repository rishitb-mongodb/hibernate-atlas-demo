package com.mongodb.demo;

/** Console output helpers. The narration lives here and in DEMO_SCRIPT.md, not in the act code. */
public final class Mql {

    private static int printCount;

    private Mql() {}

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

    /** Lets {@link Demo} tell whether an act is still commented out. */
    public static int printCount() {
        return printCount;
    }
}
