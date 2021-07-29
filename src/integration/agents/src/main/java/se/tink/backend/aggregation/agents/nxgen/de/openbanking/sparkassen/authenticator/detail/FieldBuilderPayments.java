package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields.ScaMethodEntityToIconMapper;
import se.tink.backend.aggregation.agents.utils.supplementalfields.TanBuilder;
import se.tink.libraries.i18n.Catalog;

public class FieldBuilderPayments extends FieldBuilder {
    private static final Pattern BANK_INSTRUCTIONS_PATTERN = Pattern.compile("Stecken.*");
    private static final String DELIMETER = "\\.\\s";

    public FieldBuilderPayments(Catalog catalog, ScaMethodEntityToIconMapper iconUrlMapper) {
        super(catalog, iconUrlMapper);
    }

    @Override
    public List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = new LinkedList<>();
        TanBuilder tanBuilder = prepareTanBuilder(scaMethod);

        if (challengeData != null) {
            tanBuilder.otpFormat(challengeData.getOtpFormat());
        }

        if (CHIP_TYPE.equalsIgnoreCase(scaMethod.getAuthenticationType())) {
            if (challengeData == null) {
                throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.INSTRUCTIONS_NOT_FOUND);
            }
            return GermanFields.Startcode.buildWithTemplate(
                    catalog,
                    retrieveInstructions(challengeData.getAdditionalInformation()),
                    retrieveStartCode(challengeData.getAdditionalInformation()),
                    CommonFields.convertFieldToCommonInput(tanBuilder.build()));
        }
        fields.add(tanBuilder.build());
        return fields;
    }

    private List<String> retrieveInstructions(String additionalInformation) {
        Matcher matcher = BANK_INSTRUCTIONS_PATTERN.matcher(additionalInformation);
        if (matcher.find()) {
            String instructions = matcher.group(0);
            return Arrays.asList(instructions.split(DELIMETER));
        }
        throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.INSTRUCTIONS_NOT_FOUND);
    }
}
