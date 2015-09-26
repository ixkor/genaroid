package net.xkor.genaroid;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Utils {
    public static String getStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
