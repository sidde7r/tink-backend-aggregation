package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DefaultEmbeddedFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

public class PostbankEmbeddedFieldBuilder extends DefaultEmbeddedFieldBuilder {

    public PostbankEmbeddedFieldBuilder(
            Catalog catalog, GermanFields.ScaMethodEntityToIconMapper iconUrlMapper) {
        super(
                catalog,
                iconUrlMapper,
                buildStartcodeExtractor(PostbankConstants.Patterns.STARTCODE_CHIP_PATTERN));
    }
}
