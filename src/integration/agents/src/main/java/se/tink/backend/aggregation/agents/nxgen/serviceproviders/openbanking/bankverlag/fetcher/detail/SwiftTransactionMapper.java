package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.detail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail.SwiftDateParser;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SwiftTransactionMapper {

    private static final Pattern RE_MT940_HEADER_PATTERN =
            Pattern.compile("(^\\d{6})(\\d{4})?(D|C|RC|RD)\\D?(\\d*,\\d*)(N.*)");
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("(\\d\\d)(.*)");

    private static final String REF_CUSTOMER = "KREF+";
    private static final String REF_MANDATE = "MREF+";
    private static final String REF_E2E = "EREF+";
    private static final String REF_SEPA = "SVWZ+";

    @AllArgsConstructor
    protected static class RawMT940Transaction {
        private String header;
        private String details;
    }

    @AllArgsConstructor
    private static class Segment {
        private int key;
        private String value;
    }

    private static final String MT940_HEADER_FIELD = ":61:";
    private static final String MT940_DETAILS_FIELD = ":86:";

    public List<AggregationTransaction> parse(String rawMT940) {
        List<RawMT940Transaction> rawTransactions = extractRawTransactions(rawMT940);
        return rawTransactions.stream().map(this::toTinkTransaction).collect(Collectors.toList());
    }

    private List<RawMT940Transaction> extractRawTransactions(String rawMT940) {
        List<RawMT940Transaction> transactions = new ArrayList<>();

        try (Scanner scanner = new Scanner(rawMT940); ) {
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                while (line.startsWith(MT940_HEADER_FIELD)) {
                    String header = line.substring(4);

                    StringBuilder detailsBuilder = new StringBuilder();
                    line = extractRawTransactionDetails(scanner, detailsBuilder);
                    String details = detailsBuilder.toString();

                    transactions.add(new RawMT940Transaction(header, details));
                }
            }
        }
        return transactions;
    }

    private String extractRawTransactionDetails(Scanner scanner, StringBuilder detailsBuilder) {
        if (!scanner.hasNextLine()) {
            return "";
        }

        // Skip any line that doesn't start the :86: section
        String line = scanner.nextLine();
        while (!line.startsWith(MT940_DETAILS_FIELD)) {
            line = scanner.nextLine();
        }

        // Collect content of :86: section for further processing
        detailsBuilder.append(line.substring(4));
        while (scanner.hasNextLine() && !(line = scanner.nextLine()).startsWith(":")) {
            detailsBuilder.append(line);
        }

        // We need to return already consumed line from scanner for futher processing. It could be
        // the :61:
        return line;
    }

    protected AggregationTransaction toTinkTransaction(RawMT940Transaction rawMT940Transaction) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                getAmount(rawMT940Transaction), FinTsConstants.CURRENCY))
                .setDate(getDate(rawMT940Transaction))
                .setTransactionReference(getReference(rawMT940Transaction))
                .setDescription(getDescription(rawMT940Transaction))
                .build();
    }

    protected BigDecimal getAmount(RawMT940Transaction rawMT940Transaction) {
        Matcher matcher = RE_MT940_HEADER_PATTERN.matcher(rawMT940Transaction.header);
        boolean isNegative = true;
        if (matcher.find()) {
            if (matcher.group(3).contains("C")) {
                isNegative = false;
            }
            BigDecimal value = new BigDecimal(matcher.group(4).replace(",", "."));
            return isNegative ? value.negate() : value;
        } else {
            throw new IllegalStateException("Could not parse transaction header properly.");
        }
    }

    protected Date getDate(RawMT940Transaction rawMT940Transaction) {
        LocalDate date = SwiftDateParser.parseDate(rawMT940Transaction.header);
        return SwiftDateParser.toDate(date);
    }

    protected String getReference(RawMT940Transaction rawMT940Transaction) {
        Matcher matcher = RE_MT940_HEADER_PATTERN.matcher(rawMT940Transaction.header);
        if (matcher.find()) {
            String referenceGroup = matcher.group(5);
            return referenceGroup
                    .replaceFirst("NTRFNONREF", "Keine Referenz SEPA")
                    .replaceFirst("NTRF", "Transaction number:");
        } else {
            return null;
        }
    }

    protected String getDescription(RawMT940Transaction rawMT940Transaction) {
        List<Segment> segments =
                getSegments(rawMT940Transaction).stream()
                        .filter(segment -> isRelevant(segment.key))
                        .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        for (Segment segment : segments) {
            String transformedValue = transformValue(segment.value);

            builder.append(transformedValue);

            // 27 is max length of a value in a single segment.
            // If this value did not hit 27, we probably need a separator.
            // If transformed value was empty, we do not want a separator, as it leads to multiple
            // spaces in a row.
            if (segment.value.length() < 27 && !transformedValue.equals("")) {
                builder.append(" ");
            }
        }

        return builder.toString().trim();
    }

    protected List<Segment> getSegments(RawMT940Transaction rawMT940Transaction) {
        List<Segment> segments = new ArrayList<>();
        String[] rawSegments = rawMT940Transaction.details.split("\\?");

        for (String rawSegment : rawSegments) {
            Matcher matcher = SEGMENT_PATTERN.matcher(rawSegment);
            if (matcher.find()) {
                int key = Integer.parseInt(matcher.group(1));
                String value = matcher.group(2);
                segments.add(new Segment(key, value));
            }
        }

        return segments;
    }

    private boolean isRelevant(int key) {
        // ?2x codes contain transaction description
        // ?3x codes contain bank information
        // For now we are only interested in transaction description
        return key >= 20 && key <= 29;
    }

    private String transformValue(String value) {
        // We only transform those who start with known prefixes. Ignore the rest, leave them as-is.
        if (!value.startsWith(REF_CUSTOMER)
                && !value.startsWith(REF_MANDATE)
                && !value.startsWith(REF_E2E)
                && !value.startsWith(REF_SEPA)) {
            return value;
        }

        // If nothing apart from NONREF comes after known prefix. skip the value totally.
        if (value.substring(REF_CUSTOMER.length()).equals("NONREF")) {
            return "";
        }

        // For all other cases, replace prefix with human readable string, and return
        return value.replace(REF_CUSTOMER, "Kundenreferenz: ")
                .replace(REF_MANDATE, "Mandatreferenz: ")
                .replace(REF_E2E, "End-to-End Referenz: ")
                .replace(REF_SEPA, "Sepa Transfer: ");
    }
}
