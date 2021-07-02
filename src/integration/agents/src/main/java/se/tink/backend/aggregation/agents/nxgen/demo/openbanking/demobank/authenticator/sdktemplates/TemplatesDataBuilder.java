package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.sdktemplates;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.dto.AppCodeData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.dto.CardReaderData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonPositionalInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.InGroup;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledWithChangeMethodData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.dto.IdCompletionData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto.SmsCodeData;

@UtilityClass
public class TemplatesDataBuilder {

    private static final String ICON_URL = "https://cdn.tink.se/provider-images/demobank.png";
    private static final String ENTER_YOUR_CODE = "Enter your code";

    public static AppCodeData prepareAppCodeData(String otpCode) {
        CommonInput commonInput = getCommonInput(otpCode);
        return AppCodeData.builder()
                .iconUrl(ICON_URL)
                .title(ENTER_YOUR_CODE)
                .instructions(getInstructions(otpCode))
                .input(commonInput)
                .build();
    }

    public static CardReaderData prepareCardReaderData(String otpCode) {
        CommonInput commonInput = getCommonInput(otpCode);

        return CardReaderData.builder()
                .instructions(getInstructions(otpCode))
                .input(commonInput)
                .secondFactorDescription("Generated code")
                .secondFactorValue(otpCode)
                .build();
    }

    public static DecoupledData prepareDecoupledData() {
        return DecoupledData.builder().iconUrl(ICON_URL).text("Click continue to proceed").build();
    }

    public static DecoupledWithChangeMethodData prepareDecoupledWithChangeMethodData() {
        return DecoupledWithChangeMethodData.builder()
                .iconUrl(ICON_URL)
                .text("Click continue to proceed")
                .buttonText("Change method")
                .build();
    }

    public static IdCompletionData prepareIdCompletionData() {
        CommonInput commonInput =
                CommonInput.builder()
                        .description("Password")
                        .sensitive(true)
                        .inputFieldHelpText("Please provide password (6-12 chars)")
                        .inputFieldMaxLength(12)
                        .inputFieldMinLength(6)
                        .inputFieldPattern("\\%s")
                        .inputFieldPatternError("Invalid password (6-12 chars)")
                        .build();

        CommonPositionalInput positionalInput1 =
                CommonPositionalInput.builder()
                        .description("SSN")
                        .inputFieldHelpText("Enter the missing digits from your SSN")
                        .inputFieldMaxLength(10)
                        .inputFieldMinLength(10)
                        .inputFieldPattern("^[0-9]{10}$")
                        .inputFieldPatternError("Please provide correct SSN (10 digits)")
                        .inGroup(InGroup.builder().group("Identification").oneOf(true).build())
                        .positionOfFieldsToHide(Arrays.asList(0, 2, 6, 7, 8))
                        .build();

        CommonPositionalInput positionalInput2 =
                CommonPositionalInput.builder()
                        .description("ID")
                        .inputFieldHelpText("Enter the missing characters from your ID")
                        .inputFieldMaxLength(10)
                        .inputFieldMinLength(10)
                        .inputFieldPattern("^[a-zA-Z0-9]{10}$")
                        .inputFieldPatternError("Please provide correct SSN (10 characters)")
                        .inGroup(InGroup.builder().group("Identification").oneOf(true).build())
                        .positionOfFieldsToHide(Arrays.asList(1, 4, 5, 9))
                        .build();

        List<CommonPositionalInput> positionalInputs =
                Arrays.asList(positionalInput1, positionalInput2);

        return IdCompletionData.builder()
                .colorHex("#4a4e37")
                .identityHintText("UserID: 12312345")
                .identityHintImage(ICON_URL)
                .passwordInput(commonInput)
                .title("Identifiy towards the bank")
                .identifications(positionalInputs)
                .build();
    }

    public static SmsCodeData prepareSmsCodeData(String otpCode) {
        CommonInput commonInput = getCommonInput(otpCode);

        return SmsCodeData.builder()
                .iconUrl(ICON_URL)
                .title(ENTER_YOUR_CODE)
                .instructions(getInstructions(otpCode))
                .input(commonInput)
                .build();
    }

    private static List<String> getInstructions(String otpCode) {
        return Arrays.asList("Verify it otp codes matches", "Provide code: " + otpCode);
    }

    private static CommonInput getCommonInput(String otpCode) {
        int otpLength = otpCode.length();
        return CommonInput.builder()
                .description("Otp code")
                .inputFieldHelpText("Provide code: " + otpCode)
                .inputFieldMaxLength(otpLength)
                .inputFieldMinLength(otpLength)
                .inputFieldPattern("^[0-9]{" + otpLength + "}")
                .inputFieldPatternError("You need to provide " + otpLength + "digits otp code")
                .build();
    }
}
