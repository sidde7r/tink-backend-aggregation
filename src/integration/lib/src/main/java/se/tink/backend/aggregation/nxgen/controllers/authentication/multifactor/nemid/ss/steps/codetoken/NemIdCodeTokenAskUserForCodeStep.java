package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_SERIAL_NUMBER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import com.google.inject.Inject;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdCodeTokenAskUserForCodeStep {

    private static final Pattern VALID_SERIAL_NUMBER_REGEX =
            Pattern.compile("^\\d{4} \\d{4} \\d{4} \\d{4}$");
    private static final Pattern VALID_CODE_REGEX = Pattern.compile("^\\d{6}$");

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final Catalog catalog;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;

    public String askForCodeAndValidateResponse(Credentials credentials) {
        return metrics.executeWithTimer(
                () -> {
                    String tokenSerialNumber = getCodeTokenSerialNumber();
                    verifySerialNumber(tokenSerialNumber);

                    String code = askUserForCode(tokenSerialNumber, credentials);
                    verifyCode(code);
                    return code;
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private String getCodeTokenSerialNumber() {
        return driverWrapper
                .tryFindElement(NEMID_CODE_TOKEN_SERIAL_NUMBER)
                .map(WebElement::getText)
                .map(String::trim)
                .orElseThrow(
                        () -> new IllegalStateException("Cannot find NemId token serial number"));
    }

    private void verifySerialNumber(String serialNumber) {
        if (valueDoesNotMatchPattern(serialNumber, VALID_SERIAL_NUMBER_REGEX)) {
            throw new IllegalStateException(
                    String.format("Invalid NemId code token serial number: \"%s\"", serialNumber));
        }
    }

    private String askUserForCode(String codeTokenSerialNumber, Credentials credentials) {
        Field codeInfoField = CommonFields.CodeTokenInfo.build(catalog, codeTokenSerialNumber);
        Field codeValueField = CommonFields.CodeTokenCode.build(catalog);

        statusUpdater.updateStatusPayload(
                credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_TOKEN_CODE);
        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        codeInfoField, codeValueField);

        return supplementalInfoResponse.get(CommonFields.CodeTokenCode.FIELD_KEY);
    }

    private void verifyCode(String code) {
        if (valueDoesNotMatchPattern(code, VALID_CODE_REGEX)) {
            String errorMessage =
                    NemIdConstants.NEM_ID_PREFIX + "Invalid code token code format:" + code;
            throw NemIdError.INVALID_CODE_TOKEN_CODE.exception(errorMessage);
        }
    }

    private boolean valueDoesNotMatchPattern(String value, Pattern pattern) {
        return !pattern.matcher(value).matches();
    }
}
