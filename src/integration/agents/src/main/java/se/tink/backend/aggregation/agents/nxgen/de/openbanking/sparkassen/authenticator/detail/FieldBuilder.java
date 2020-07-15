package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class FieldBuilder {

    private static final String OTP_VALUE_FIELD_KEY = "otpValue";
    private static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
    private static final Pattern STARTCODE_CHIP_PATTERN =
            Pattern.compile("(?<= Startcode\\s).[\\d]+");

    private Catalog catalog;

    public FieldBuilder(Catalog catalog) {
        this.catalog = catalog;
    }

    public Field getOtpField(String otpType, int otpValueLength, String additionalInformation)
            throws LoginException {

        return Field.builder()
                .description(
                        this.catalog.getString(getOtpDescription(otpType, additionalInformation)))
                .helpText(otpType)
                .name(OTP_VALUE_FIELD_KEY)
                .numeric(true)
                .minLength(otpValueLength)
                .maxLength(otpValueLength)
                .hint(StringUtils.repeat("N", otpValueLength))
                .pattern(String.format("([0-9]{%d})", otpValueLength))
                .patternError("The code you entered is not valid")
                .build();
    }

    private String getOtpDescription(String otpType, String additionalInformation)
            throws LoginException {
        if (("CHIP_OTP").equals(otpType)) {
            String startCode = retrieveStartCode(additionalInformation);
            return String.format(
                    "Please insert card to TAN-Generator and press “TAN”. Then insert start code %s and press “OK”",
                    startCode);
        }
        return otpType;
    }

    private String retrieveStartCode(String additionalInformation) throws LoginException {
        Matcher matcher = STARTCODE_CHIP_PATTERN.matcher(additionalInformation);
        if (!matcher.find()) {
            throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.STARTCODE_NOT_FOUND);
        }
        return matcher.group();
    }

    public Field getChooseScaMethodField(List<ScaMethodEntity> scaMethods) {
        int maxNumber = scaMethods.size();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(i -> prettyPrintScaMethodWithIndex(scaMethods.get(i), i))
                        .collect(Collectors.joining(";\n"));

        return Field.builder()
                .description(this.catalog.getString(description))
                .helpText("Please select SCA method")
                .name(CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .maxLength(Integer.toString(maxNumber).length())
                .hint(String.format("Select from 1 to %d", maxNumber))
                .pattern(RangeRegex.regexForRange(1, maxNumber))
                .patternError("The chosen SCA method is not valid")
                .build();
    }

    private String prettyPrintScaMethodWithIndex(ScaMethodEntity scaMethod, int index) {
        return String.format(
                "(%d) Name: %s, Type: %s",
                index + 1, scaMethod.getName(), scaMethod.getAuthenticationType());
    }
}
