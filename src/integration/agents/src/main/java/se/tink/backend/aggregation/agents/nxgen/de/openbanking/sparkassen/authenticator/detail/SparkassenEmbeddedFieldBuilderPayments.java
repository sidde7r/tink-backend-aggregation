package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Patterns.BANK_INSTRUCTIONS_DELIMITER;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Patterns.BANK_INSTRUCTIONS_PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Patterns.STARTCODE_CHIP_PATTERN;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DefaultEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.SdkTemplatesEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

// Temporary solution. We need to utilize SDK Templates for payment chiptan auth as soon as
// possible,
// even before we have time to move rest of supplemental info interactions to SDK Templates.
// The goal is to have just one FieldBuilder for embedded interactions for this Agent.
public class SparkassenEmbeddedFieldBuilderPayments extends SdkTemplatesEmbeddedFieldBuilder {

    static final Function<String, List<String>> INSTRUCTION_EXTRACTOR =
            input -> {
                if (input == null) {
                    return Collections.emptyList();
                }
                Matcher matcher = BANK_INSTRUCTIONS_PATTERN.matcher(input);
                if (matcher.find()) {
                    String instructions = matcher.group(0);
                    return Arrays.asList(BANK_INSTRUCTIONS_DELIMITER.split(instructions));
                } else {
                    return Collections.emptyList();
                }
            };

    public SparkassenEmbeddedFieldBuilderPayments(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                DefaultEmbeddedFieldBuilder.buildStartcodeExtractor(STARTCODE_CHIP_PATTERN),
                INSTRUCTION_EXTRACTOR);
    }
}
