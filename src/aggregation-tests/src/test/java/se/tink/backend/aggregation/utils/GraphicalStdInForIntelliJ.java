package se.tink.backend.aggregation.utils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class GraphicalStdInForIntelliJ extends InputStream {
    private static String message;
    String prevMessage = "";
    byte[] contents;
    int pointer = 0;

    @Override
    public int read() throws IOException {
        // Multiple reads can occur even after this method has returned -1.
        // Compare the message text to avoid prompting multiple times.
        if (!prevMessage.equals(message)) {
            contents = JOptionPane.showInputDialog(message).getBytes();
            pointer = 0;
            prevMessage = message;
        }
        if (pointer >= contents.length) {
            return -1;
        }
        return this.contents[pointer++];
    }

    public static void apply()
    {
        System.setIn(new GraphicalStdInForIntelliJ());
    }

    public static void setMessage(String message) {
        GraphicalStdInForIntelliJ.message = message;
    }
}
