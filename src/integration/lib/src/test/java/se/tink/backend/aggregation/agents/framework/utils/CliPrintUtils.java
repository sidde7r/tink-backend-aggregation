package se.tink.backend.aggregation.agents.framework.utils;

import com.google.common.base.Strings;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CliPrintUtils {
    // To get UTF-8 output.
    private static final PrintStream out = initOutput();

    private static final int INFINITE_MAX_ROWS = 0;
    private static final String NULL_VALUE = "<null>";
    private static final String ROW_START = "| ";
    private static final String ROW_END = " |";

    private static PrintStream initOutput() {
        try {
            return new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void printTable(
            int indentation, String tableName, List<Map<String, String>> rows) {
        printTable(indentation, tableName, rows, INFINITE_MAX_ROWS);
    }

    // This method encapsulates the title with maxLength of character `c`.
    // I.e. encapsuleTitle("test", 10, '-') --> "-- test --"
    private static String encapsuleTitle(String title, int maxLength, char c) {
        title = String.format(" %s ", title);
        if (title.length() > maxLength) {
            title = title.substring(0, maxLength - 3) + "...";
        }
        int headerStart = (maxLength / 2) - (title.length() / 2);
        String header = Strings.repeat(String.valueOf(c), headerStart) + title;
        return Strings.padEnd(header, maxLength, c);
    }

    public static void printTable(
            int indentation, String tableName, List<Map<String, String>> rows, int maxRows) {
        if (rows.isEmpty()) {
            return;
        }

        // Populating max width of each column

        final Map<String, Integer> columnWidths = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> col : row.entrySet()) {
                if (col.getValue() == null) {
                    col.setValue(NULL_VALUE);
                }
                columnWidths.put(
                        col.getKey(),
                        Math.max(
                                Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0),
                                col.getValue().length()));
            }
        }
        for (Map.Entry<String, Integer> col : columnWidths.entrySet()) {
            columnWidths.put(
                    col.getKey(),
                    Math.max(
                            Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0),
                            col.getKey().length()));
        }

        // Calculating total width of table.

        int totalWidth = 4 + (columnWidths.size() - 1) * 3;
        for (Map.Entry<String, Integer> col : columnWidths.entrySet()) {
            totalWidth += col.getValue();
        }

        // Print table.
        out.println(Strings.repeat(" ", indentation) + encapsuleTitle(tableName, totalWidth, '='));

        // Print column headers

        out.print(Strings.repeat(" ", indentation) + ROW_START);
        boolean first = true;
        for (String key : columnWidths.keySet()) {
            if (!first) {
                out.print(" | ");
            } else {
                first = false;
            }

            out.print(key);
            out.print(Strings.repeat(" ", columnWidths.get(key) - key.length())); // Padding
        }
        out.println(ROW_END);

        out.print(Strings.repeat(" ", indentation) + "|");
        out.print(Strings.repeat("-", totalWidth - 2));
        out.println("|");

        // Print data

        if ((maxRows > 0) && (maxRows < rows.size())) {
            // do (maxRows/2) first and (maxRows/2) last.

            int segmentCount = maxRows / 2;

            // from start
            for (int i = 0; i < segmentCount; i++) {
                Map<String, String> row = rows.get(i);
                printRow(indentation, row, columnWidths);
            }

            String skippedRowsMessage =
                    String.format("Skipped rows: %d", rows.size() - segmentCount * 2);
            out.println(
                    Strings.repeat(" ", indentation)
                            + ROW_START
                            + encapsuleTitle(skippedRowsMessage, totalWidth - 4, '~')
                            + ROW_END);

            // from end
            for (int i = segmentCount; i > 0; i--) {
                Map<String, String> row = rows.get(rows.size() - i);
                printRow(indentation, row, columnWidths);
            }
        } else {
            for (Map<String, String> row : rows) {
                printRow(indentation, row, columnWidths);
            }
        }

        // Print footer
        out.println(Strings.repeat(" ", indentation) + Strings.repeat("=", totalWidth));
    }

    private static void printRow(
            int indentation, Map<String, String> row, final Map<String, Integer> columnWidths) {
        out.print(Strings.repeat(" ", indentation) + ROW_START);
        boolean first = true;
        for (Map.Entry<String, Integer> column : columnWidths.entrySet()) {
            if (!first) {
                out.print(" | ");
            } else {
                first = false;
            }
            String value = Optional.ofNullable(row.get(column.getKey())).orElse("");
            value =
                    value.replace(
                            "\n",
                            "⏎"); // Newlines ruin the table; replaced string needs to be a single
            // char
            out.print(value);
            out.print(Strings.repeat(" ", column.getValue() - value.length())); // Padding
        }
        out.println(ROW_END);
    }

    public static String formatPercent(Double number) {
        if (number != null) {
            return number.doubleValue() * 100 + "%";
        }
        return "null";
    }
}
