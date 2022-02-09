package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.detail;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DefaultEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

public class UnicreditEmbeddedFieldBuilder extends DefaultEmbeddedFieldBuilder {

    public UnicreditEmbeddedFieldBuilder(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                buildStartcodeExtractor(UnicreditConstants.Patterns.STARTCODE_CHIP_PATTERN));
    }
}
