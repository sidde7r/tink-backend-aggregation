package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.utils;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class DutchTextCleaner {

    public static final ImmutableList<String> DUCTH_DESC_REMOVE_LIST =
            ImmutableList.of(
                    "pasvolgnr",
                    "transactie",
                    "valutadatum",
                    "valuta",
                    "per.",
                    "ing-id",
                    "iban",
                    "machtiging",
                    "incassant",
                    "omschrijving",
                    "overige partij",
                    "kenmerk",
                    "datum",
                    "factuur",
                    "euro",
                    "id-",
                    "voor",
                    "periode",
                    "t/m",
                    "kaartnummer");

    public String clean(String description) {

        StringBuilder cleanString = new StringBuilder();
        if (description.contains("<br>")) {
            description = description.replaceAll("<br>", " | ");
        } else if (description.contains("\\n")) {
            description = description.replaceAll("\\n", " | ");
        }

        if (description.contains("|")) {
            String[] split = description.split("\\|");
            for (String str : split) {
                String removedStr = removeUnNeededText(str.toLowerCase());
                if (!removedStr.isEmpty()) {
                    cleanString = cleanString.append(removedStr).append(" | ");
                }
            }
        }
        return StringUtils.chop(cleanString.toString().trim()).trim();
    }

    private static String removeUnNeededText(final String text) {

        String partlyCleaned =
                DUCTH_DESC_REMOVE_LIST.stream().anyMatch(text::contains) ? null : text;

        List<String> cleanString = new ArrayList<>();
        if (partlyCleaned != null) {
            List<String> strings = Arrays.asList(partlyCleaned.split("\\s"));

            cleanString =
                    strings.stream()
                            .map(
                                    s ->
                                            s.replaceAll(
                                                    "([0-9]{2}).([0-9]{2}).([0-9]{4})",
                                                    "")) // remove date format
                            .map(s -> s.replaceAll("^\\d+(?:[.,-]\\d+)*$", ""))
                            // removed digit only
                            .map(s -> s.replaceAll("naam:", "")) // removed word "naam:"
                            .collect(Collectors.toList());
        }
        return String.join(" ", cleanString);
    }
}
