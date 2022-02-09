package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DefaultEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

public class SparkassenEmbeddedFieldBuilder extends DefaultEmbeddedFieldBuilder {
    public SparkassenEmbeddedFieldBuilder(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                DefaultEmbeddedFieldBuilder.buildStartcodeExtractor(
                        SparkassenConstants.Patterns.STARTCODE_CHIP_PATTERN));
    }
}
