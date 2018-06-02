package se.tink.backend.categorization.rules;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Transaction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MerchantMappingCommand implements Classifier {
    @VisibleForTesting static final String ROW_SPLITTER = ";";
    private final Map<String, String> merchantMap;
    private static final Logger log = LoggerFactory.getLogger(MerchantMappingCommand.class);

    private MerchantMappingCommand(Map<String, String> merchantMap) {
        this.merchantMap = merchantMap;
    }

    public static Optional<MerchantMappingCommand> build(Optional<String> filePath) {
        return filePath.flatMap(p -> {
            try (BufferedReader resource = new BufferedReader(new InputStreamReader(new FileInputStream(p),
                    StandardCharsets.UTF_8))) {
                return Optional.of(new MerchantMappingCommand(loadMerchantMap(resource)));
            } catch (IOException e) {
                log.warn("Cannot load merchant mapping", e);
                return Optional.empty();
            }
        });
    }

    private static Map<String, String> loadMerchantMap(BufferedReader bufferedReader) {
        return bufferedReader.lines()
                .map(l -> l.split(ROW_SPLITTER))
                .peek(columns -> {
                    if (columns.length != 2) {
                        throw new IllegalStateException(
                                String.format("Cell count in merchants file mismatch: 2 != %s", columns.length));
                    }
                })
                .collect(Collectors.toMap(columns -> columns[0].trim().toLowerCase(), row -> row[1]));
    }

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {
        return Optional.ofNullable(merchantMap.get(transaction.getDescription()))
                .map(cat -> new Outcome(CategorizationCommand.MERCHANT_MAP,
                        new CategorizationVector(CategorizationWeight.MERCHANT_MAPPING, cat, 1)));
    }

}
