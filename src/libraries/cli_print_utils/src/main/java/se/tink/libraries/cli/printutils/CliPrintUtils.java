package se.tink.libraries.cli.printutils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CliPrintUtils {

    public static Map<String, String> keyValueEntry(String key, String value) {
        Map<String, String> entry = Maps.newLinkedHashMap();
        entry.put("Property", key);
        entry.put("Value", value);
        return entry;
    }

    public static void printTable(List<Map<String, String>> rows) {

        if (rows.isEmpty()) {
            System.out.println("<no data>");
            return;
        }

        // Populating max width of each column

        final Map<String, Integer> columnWidths = Maps.newLinkedHashMap();
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> col : row.entrySet()) {
                if (col.getValue() == null) {
                    col.setValue("<null>");
                }
                columnWidths.put(col.getKey(),
                        Math.max(Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0), col.getValue().length
                                ()));
            }
        }
        for (Map.Entry<String, Integer> col : columnWidths.entrySet()) {
            columnWidths.put(col.getKey(),
                    Math.max(Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0), col.getKey().length()));
        }

        // Calculating total width of table.

        int totalWidth = 4 + (columnWidths.size() - 1) * 3;
        for (Map.Entry<String, Integer> col : columnWidths.entrySet()) {
            totalWidth += col.getValue();
        }

        // Print table.

        System.out.println(Strings.repeat("=", totalWidth));

        // Print column headers

        System.out.print("| ");
        boolean first = true;
        for (String key : columnWidths.keySet()) {
            if (!first) {
                System.out.print(" | ");
            } else {
                first = false;
            }

            System.out.print(key);
            System.out.print(Strings.repeat(" ", columnWidths.get(key) - key.length())); // Padding
        }
        System.out.println(" |");

        System.out.print("|");
        System.out.print(Strings.repeat("-", totalWidth - 2));
        System.out.println("|");

        // Print data

        for (Map<String, String> row : rows) {
            System.out.print("| ");
            first = true;
            for (Map.Entry<String, Integer> column : columnWidths.entrySet()) {
                if (!first) {
                    System.out.print(" | ");
                } else {
                    first = false;
                }
                String value = Optional.ofNullable(row.get(column.getKey())).orElse("");
                System.out.print(value);
                System.out.print(Strings.repeat(" ", column.getValue() - value.length())); // Padding
            }
            System.out.println(" |");
        }

        // Print footer

        System.out.println(Strings.repeat("=", totalWidth));
    }

}
