package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
// Default implementation, that is also customizable, to be easily reusable by many DE Agents.
// This implementation DOES NOT use SDK Templates.
public class DefaultEmbeddedFieldBuilder implements EmbeddedFieldBuilder {

    private static final String STARTCODE_NOT_FOUND = "Startcode for Chip tan not found";
    protected static final String CHIP_TYPE = "CHIP_OTP";

    protected final Catalog catalog;
    private final GermanFields.ScaMethodEntityToIconMapper iconUrlMapper;

    private final Function<String, Optional<String>> startcodeExtractor;

    @Override
    public Field getChooseScaMethodField(List<ScaMethodEntity> scaMethods) {
        return CommonFields.Selection.build(
                catalog,
                null,
                GermanFields.SelectOptions.prepareSelectOptions(scaMethods, iconUrlMapper));
    }

    @Override
    public List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = new LinkedList<>();

        if (CHIP_TYPE.equalsIgnoreCase(scaMethod.getAuthenticationType())) {
            fields.add(
                    GermanFields.Startcode.build(
                            catalog, retrieveStartCode(challengeData.getAdditionalInformation())));
        }

        fields.add(prepareTanField(scaMethod, challengeData));
        return fields;
    }

    protected Field prepareTanField(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        return GermanFields.Tan.builder(catalog)
                .authenticationType(scaMethod.getAuthenticationType())
                .authenticationMethodName(scaMethod.getName())
                .otpMinLength(6)
                .otpMaxLength(6)
                .otpFormat(challengeData != null ? challengeData.getOtpFormat() : null)
                .build();
    }

    protected String retrieveStartCode(String additionalInformation) {
        return startcodeExtractor
                .apply(additionalInformation)
                .orElseThrow(() -> LoginError.NOT_SUPPORTED.exception(STARTCODE_NOT_FOUND));
    }

    // Helper function for creating typical extractor function, reusable for different patterns
    public static Function<String, Optional<String>> buildStartcodeExtractor(Pattern pattern) {
        return input -> {
            if (input == null) {
                return Optional.empty();
            }
            Matcher matcher = pattern.matcher(input);
            return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
        };
    }
}
