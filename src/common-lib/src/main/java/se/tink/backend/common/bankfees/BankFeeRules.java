package se.tink.backend.common.bankfees;

import com.google.common.base.Charsets;
import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radixinverted.ConcurrentInvertedRadixTree;
import com.googlecode.concurrenttrees.radixinverted.InvertedRadixTree;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.core.BankFeeType;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

public class BankFeeRules {

    private static final BankFeeRules instance = new BankFeeRules();

    private static final LogUtils log = new LogUtils(BankFeeRules.class);

    private final InvertedRadixTree<BankFeeType> wildCardTree = new ConcurrentInvertedRadixTree<>(
            new DefaultCharArrayNodeFactory());
    private final RadixTree<BankFeeType> exactMatchTree = new ConcurrentRadixTree<>(
            new DefaultCharArrayNodeFactory());

    private BankFeeRules() {
        initializeRules();
    }

    private void initializeRules() {

        try {

            CSVFormat format = CSVFormat.newFormat(';')
                    .withRecordSeparator('\n')
                    .withCommentMarker('#')
                    .withIgnoreEmptyLines(true);

            File file = new File("data/seeding/bank-fee-rules.txt");

            // Override file-path to working directory if rules are instantiated from Spark.
            if (!file.isFile()) {
                file = new File("bank-fee-rules.txt");
            }

            CSVParser s = CSVParser.parse(file, Charsets.UTF_16, format);

            for (CSVRecord record : s.getRecords()) {
                String regexp = record.get(0).toUpperCase();
                BankFeeType type = BankFeeType.valueOf(record.get(1));

                if (regexp.endsWith("*")) {
                    wildCardTree.put(StringUtils.stripEnd(regexp, "*"), type);
                } else {
                    exactMatchTree.put(regexp, type);
                }
            }

        } catch (IOException e) {
            log.error("Could not load bank fee rules", e);
        }
    }

    public boolean matches(String description) {
        return matchDetails(description).matches;
    }

    public boolean matches(Transaction transaction) {
        return matchDetails(transaction.getDescription()).matches;
    }

    public MatchResult matchDetails(String description) {
        MatchResult result = new MatchResult();

        if (description == null) {
            return result;
        }

        String lookupKey = description.toUpperCase();

        // Check for exact match
        BankFeeType exactMatch = exactMatchTree.getValueForExactKey(lookupKey);

        if (exactMatch != null) {
            result.matches = true;
            result.type = exactMatch;
            return result;
        }

        // Check for wildcard match
        Optional<KeyValuePair<BankFeeType>> firstMatch = wildCardTree.getKeyValuePairsForKeysContainedIn(lookupKey)
                .stream().findFirst();

        if (firstMatch.isPresent() && firstMatch.get().getKey().length() > 0) {
            result.matches = true;
            result.type = firstMatch.get().getValue();
        }

        return result;
    }

    public static BankFeeRules getInstance() {
        return instance;
    }

    public class MatchResult {
        public BankFeeType type;
        public boolean matches;
    }

}
