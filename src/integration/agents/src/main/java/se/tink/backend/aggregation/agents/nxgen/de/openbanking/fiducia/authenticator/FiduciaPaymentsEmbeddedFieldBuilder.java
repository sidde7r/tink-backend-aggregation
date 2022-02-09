package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.SdkTemplatesEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

// This is a separate take on building fields, for the purpose of payments only.
// Payments CHIP_TAN needs fuller instructions, thus we decided to move it to SDK templates as soon
// as possible. In future all field building will be done on sdk templates, so this class (or the
// other one) will cease to exist.
public class FiduciaPaymentsEmbeddedFieldBuilder extends SdkTemplatesEmbeddedFieldBuilder {

    static final Function<String, List<String>> INSTRUCTION_EXTRACTOR =
            input -> {
                if (input == null) {
                    return Collections.emptyList();
                }

                return FiduciaConstants.Patterns.CHIP_TAN_INSTRUCTION_LINE_DELIMITER
                        .splitAsStream(input)
                        .map(line -> line.replaceFirst("^\\d+\\.\\s", ""))
                        .collect(Collectors.toList());
            };

    public FiduciaPaymentsEmbeddedFieldBuilder(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                buildStartcodeExtractor(FiduciaConstants.Patterns.STARTCODE_CHIP_PATTERN),
                INSTRUCTION_EXTRACTOR);
    }
}
