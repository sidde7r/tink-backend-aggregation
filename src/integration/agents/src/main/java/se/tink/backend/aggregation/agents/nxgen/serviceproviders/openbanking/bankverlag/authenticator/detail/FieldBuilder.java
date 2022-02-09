package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.detail;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.Fields;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.BankverlagIconUrlMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.TanBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
public class FieldBuilder {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s(\\d+)");
    private static final String CHIP_TYPE = "CHIP_OTP";

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

    public Field getChooseScaMethodField(List<ScaMethodEntity> scaMethods, String aspspId) {
        return CommonFields.Selection.build(
                catalog,
                null,
                GermanFields.SelectOptions.prepareSelectOptions(
                        scaMethods, new BankverlagIconUrlMapper(aspspId)));
    }

    public Field getInstructionsField(ScaMethodEntity scaMethod, String aspspName) {
        return CommonFields.Instruction.build(prepareInstructions(scaMethod, aspspName));
    }

    private String prepareInstructions(ScaMethodEntity scaMethod, String aspspName) {
        if (scaMethod != null) {
            return catalog.getString(
                    Fields.INSTRUCTIONS_WITH_APP_NAME, aspspName, scaMethod.getName());
        }
        return catalog.getString(Fields.INSTRUCTIONS, aspspName);
    }
}
