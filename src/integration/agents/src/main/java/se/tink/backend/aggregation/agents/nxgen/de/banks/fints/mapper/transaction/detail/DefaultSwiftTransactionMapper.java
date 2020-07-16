package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultSwiftTransactionMapper implements TransactionMapper {

    private static final Pattern RE_MT940_AMOUNT =
            Pattern.compile("(^\\d{6})(\\d{4})?(D|C|RC|RD)\\D?(\\d*,\\d*)N.*");

    @AllArgsConstructor
    protected static class RawMT940Transaction {
        private String header;
        private String details;
    }

    private static final String MT940_HEADER_FIELD = ":61:";
    private static final String MT940_DETAILS_FIELD = ":86:";
    private static final String MT940_TRANSACTION_TYPE_SUBFIELD = "00";

    // Key ?23 causes additional not wanted space in description. It's simply sign of CR
    private static final String CARRIAGE_RETURN_KEY = "?23";

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
                .setDescription(getDescription(rawMT940Transaction))
                .setRawDetails(getRawDetails(rawMT940Transaction))
                .build();
    }

    protected BigDecimal getAmount(RawMT940Transaction rawMT940Transaction) {
        Matcher m = RE_MT940_AMOUNT.matcher(rawMT940Transaction.header);
        boolean isNegative = true;
        if (m.find()) {
            if (m.group(3).contains("C")) {
                isNegative = false;
            }
            BigDecimal value = new BigDecimal(m.group(4).replace(",", "."));
            return isNegative ? value.negate() : value;
        } else {
            throw new IllegalStateException("Could not parse transaction header properly.");
        }
    }

    protected Date getDate(RawMT940Transaction rawMT940Transaction) {
        LocalDate date = SwiftDateParser.parseDate(rawMT940Transaction.header);
        return SwiftDateParser.toDate(date);
    }

    protected String getDescription(RawMT940Transaction rawMT940Transaction) {
        Map<String, String> rawDetails = getRawDetails(rawMT940Transaction);
        String transactionTitle =
                rawDetails.entrySet().stream()
                        .filter(entry -> isTransactionTitleSubfield(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.joining(" "));

        if ("".equals(transactionTitle)) {
            return rawDetails.getOrDefault(MT940_TRANSACTION_TYPE_SUBFIELD, "");
        } else {
            return transactionTitle;
        }
    }

    private boolean isTransactionTitleSubfield(String key) {
        return key.charAt(0) == '2' && key.charAt(1) >= '0' && key.charAt(1) <= '9';
    }

    protected Map<String, String> getRawDetails(RawMT940Transaction rawMT940Transaction) {
        Map<String, String> result = new TreeMap<>();

        String details = cleanFromUnusedKeys(rawMT940Transaction.details);
        String[] elements = details.split("\\?");

        for (String s : elements) {
            if (s.length() < 2) {
                continue;
            }
            String key = s.substring(0, 2);
            String value = s.substring(2);
            result.put(key, value);
        }

        return result;
    }

    private String cleanFromUnusedKeys(String details) {
        return details.replace(CARRIAGE_RETURN_KEY, "");
    }
}
