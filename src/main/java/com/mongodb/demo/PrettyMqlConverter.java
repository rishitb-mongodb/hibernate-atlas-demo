package com.mongodb.demo;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.bson.BsonDocument;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

/**
 * A logback conversion word ({@code %mql}, see {@code logback.xml}) that indents the MQL commands
 * Hibernate logs, instead of the single compacted line it produces by default.
 *
 * <p>Those log lines aren't quite valid JSON: unbound parameters are logged as a bare {@code ?}
 * (e.g. {@code {"$eq": ?}}), which no JSON parser accepts. This masks each {@code ?} with a
 * placeholder string before parsing, then swaps it back into the pretty-printed result, so the
 * output still reads as "parameter not yet bound" rather than showing a fake value.
 *
 * <p>Anything that isn't parseable even after masking (a plain log message, for instance) is
 * printed unchanged.
 */
public final class PrettyMqlConverter extends ClassicConverter {

    private static final JsonWriterSettings PRETTY =
            JsonWriterSettings.builder().outputMode(JsonMode.SHELL).indent(true).build();

    // Plain ASCII: the BSON JSON writer re-emits it unescaped, which is what makes the
    // find-and-replace below reliable. A control character survives parsing but comes back out
    // \-escaped, so the literal escape sequence -- not the character -- is what needs replacing.
    private static final String PARAMETER_PLACEHOLDER = "HQL_UNBOUND_PARAMETER_MARKER";

    @Override
    public String convert(ILoggingEvent event) {
        var message = event.getFormattedMessage();
        try {
            var parsed = BsonDocument.parse(maskParameters(message));
            return parsed.toJson(PRETTY).replace('"' + PARAMETER_PLACEHOLDER + '"', "?");
        } catch (RuntimeException e) {
            return message;
        }
    }

    /** Replaces every unquoted {@code ?} with a quoted placeholder so the text becomes valid JSON. */
    private static String maskParameters(String mql) {
        var out = new StringBuilder(mql.length() + 32);
        var inString = false;
        for (int i = 0; i < mql.length(); i++) {
            var c = mql.charAt(i);
            if (inString) {
                out.append(c);
                if (c == '\\' && i + 1 < mql.length()) {
                    out.append(mql.charAt(++i)); // copy the escaped character verbatim
                } else if (c == '"') {
                    inString = false;
                }
            } else if (c == '"') {
                inString = true;
                out.append(c);
            } else if (c == '?') {
                out.append('"').append(PARAMETER_PLACEHOLDER).append('"');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
