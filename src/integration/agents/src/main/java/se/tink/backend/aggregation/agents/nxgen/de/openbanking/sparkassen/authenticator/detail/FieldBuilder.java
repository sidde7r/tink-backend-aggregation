package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class FieldBuilder {

    private static final String OTP_VALUE_FIELD_KEY = "otpValue";
    private static final String CHOSEN_SCA_METHOD = "chosenScaMethod";

    private Catalog catalog;

    public FieldBuilder(Catalog catalog) {
        this.catalog = catalog;
    }

    public Field getOtpField(String otpType, int otpValueLength) {
        return Field.builder()
                .description(this.catalog.getString("Verification code"))
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
