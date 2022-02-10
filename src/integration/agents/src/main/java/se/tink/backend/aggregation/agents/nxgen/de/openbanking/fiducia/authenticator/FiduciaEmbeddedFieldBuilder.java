package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DefaultEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

public class FiduciaEmbeddedFieldBuilder extends DefaultEmbeddedFieldBuilder {

    public FiduciaEmbeddedFieldBuilder(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                buildStartcodeExtractor(FiduciaConstants.Patterns.STARTCODE_CHIP_PATTERN));
    }
}
