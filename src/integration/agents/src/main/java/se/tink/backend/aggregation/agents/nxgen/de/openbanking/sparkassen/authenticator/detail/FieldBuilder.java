package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class FieldBuilder {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s(\\d+)");

    private final Catalog catalog;

    public List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = new LinkedList<>();
        if (GermanFields.Tan.AuthenticationType.CHIP_OTP
                .name()
                .equalsIgnoreCase(scaMethod.getAuthenticationType())) {
            fields.add(
                    GermanFields.Startcode.build(
                            catalog, retrieveStartCode(challengeData.getAdditionalInformation())));
        }
        fields.add(
                GermanFields.Tan.build(
                        catalog,
                        GermanFields.Tan.AuthenticationType.getIfPresentOrDefault(
                                scaMethod.getAuthenticationType()),
                        scaMethod.getName(),
                        challengeData != null ? challengeData.getOtpMaxLength() : null,
                        challengeData != null ? challengeData.getOtpFormat() : null));
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
                catalog,
                scaMethods.stream().map(ScaMethodEntity::getName).collect(Collectors.toList()));
    }
}
