package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.libraries.i18n_aggregation.Catalog;

// Similar to DefaultEmbeddedFieldBuilder, this is a helper base class for encapsulating creating
// Fields for embedded supplemental info exchange.
// This implementation DOES use SOME SDK Templates. Due to a pressing need, for now it is only used
// for chipTan.
public class SdkTemplatesEmbeddedFieldBuilder extends DefaultEmbeddedFieldBuilder {
    private static final String INSTRUCTIONS_NOT_FOUND = "Instructions for Chip tan not found";

    private final Function<String, List<String>> instructionsExtractor;

    public SdkTemplatesEmbeddedFieldBuilder(
            Catalog catalog,
            GermanFields.ScaMethodEntityToIconMapper iconUrlMapper,
            Function<String, Optional<String>> startcodeExtractor,
            Function<String, List<String>> instructionsExtractor) {
        super(catalog, iconUrlMapper, startcodeExtractor);
        this.instructionsExtractor = instructionsExtractor;
    }

    @Override
    public List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        Field tanField = prepareTanField(scaMethod, challengeData);
        if (CHIP_TYPE.equalsIgnoreCase(scaMethod.getAuthenticationType())) {
            if (challengeData == null) {
                throw LoginError.NOT_SUPPORTED.exception(INSTRUCTIONS_NOT_FOUND);
            }
            return GermanFields.Startcode.buildWithTemplate(
                    catalog,
                    retrieveInstructions(challengeData.getAdditionalInformation()),
                    retrieveStartCode(challengeData.getAdditionalInformation()),
                    CommonFields.convertFieldToCommonInput(tanField));
        } else {
            return Collections.singletonList(tanField);
        }
    }

    private List<String> retrieveInstructions(String additionalInformation) {
        List<String> instructions =
                Optional.ofNullable(additionalInformation)
                        .map(instructionsExtractor)
                        .orElse(Collections.emptyList());
        if (instructions.isEmpty()) {
            throw LoginError.NOT_SUPPORTED.exception(INSTRUCTIONS_NOT_FOUND);
        } else {
            return instructions;
        }
    }
}
