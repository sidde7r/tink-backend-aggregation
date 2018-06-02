package se.tink.backend.categorization.rules;

import org.junit.Test;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class MerchantMappingCommandTest {

    @Test public void buildWhenMappingFileDoesNotExist() {
        assertFalse(MerchantMappingCommand.build(Optional.of("mappingFile")).isPresent());
    }

    @Test public void buildWhenNoMappingFileProvided() {
        assertFalse(MerchantMappingCommand.build(Optional.empty()).isPresent());
    }

    @Test public void buildWhenMappingFileIsEmpty() throws IOException {
        File tmpFile = File.createTempFile("mappingFile", null);
        assertTrue(MerchantMappingCommand.build(Optional.of(tmpFile.getAbsolutePath())).isPresent());
    }

    @Test public void categorize() throws IOException {
        File tmpFile = File.createTempFile("mappingFile", null);
        try (FileWriter fileWriter = new FileWriter(tmpFile)) {
            fileWriter.write("transaction description" + MerchantMappingCommand.ROW_SPLITTER + "categoryCode");
        }

        Transaction transaction = new Transaction();
        transaction.setDescription("transaction description");
        int value = 1;
        assertEquals(
                Optional.of(new Classifier.Outcome(CategorizationCommand.MERCHANT_MAP,
                        new CategorizationVector(
                                CategorizationWeight.MERCHANT_MAPPING,
                                "categoryCode",
                                value))),
                MerchantMappingCommand.build(Optional.of(tmpFile.getAbsolutePath()))
                        .flatMap(c -> c.categorize(transaction)));

    }

}
