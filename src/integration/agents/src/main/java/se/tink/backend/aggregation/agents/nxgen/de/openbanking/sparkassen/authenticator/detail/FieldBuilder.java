package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.OtpFormat;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.TanBuilder;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

@RequiredArgsConstructor
public class FieldBuilder {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s(\\d+)");
    private static final String CHIP_TYPE = "CHIP_OTP";
    private static final LocalizableParametrizedKey INSTRUCTIONS =
            new LocalizableParametrizedKey(
                    "Please open the S-pushTAN app on device \"{0}\" and confirm login. Then click the \"Submit\" button");

    private final Catalog catalog;

    public List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = new LinkedList<>();
        if (CHIP_TYPE.equalsIgnoreCase(scaMethod.getAuthenticationType())) {
            fields.add(
                    GermanFields.Startcode.build(
                            catalog, retrieveStartCode(challengeData.getAdditionalInformation())));
        }

        TanBuilder tanBuilder =
                GermanFields.Tan.builder(catalog)
                        .authenticationType(scaMethod.getAuthenticationType())
                        .authenticationMethodName(scaMethod.getName())
                        .otpMinLength(6)
                        .otpMaxLength(6);
        if (challengeData != null) {
            tanBuilder.otpFormat(OtpFormat.fromString(challengeData.getOtpFormat()).orElse(null));
        }
        fields.add(tanBuilder.build());

        return fields;
    }

    private String retrieveStartCode(String additionalInformation) {
        Matcher matcher = STARTCODE_CHIP_PATTERN.matcher(additionalInformation);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.STARTCODE_NOT_FOUND);
    }

    public Field getChooseScaMethodField(List<ScaMethodEntity> scaMethods) {
        return CommonFields.Selection.build(
                catalog, null, GermanFields.SelectOptions.prepareSelectOptions(scaMethods));
    }

    public Field getInstructionsField(ScaMethodEntity scaMethod) {
        return CommonFields.Instruction.build(catalog.getString(INSTRUCTIONS, scaMethod.getName()));
    }
}
