package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_CODE_NUMBER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_NUMBER;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdCodeCardAskUserForCodeStep {

    public static final Integer EXPECTED_CODE_LENGTH = 6;

    private static final Pattern VALID_CODE_CARD_NUMBER_REGEX =
            Pattern.compile("^[a-zA-Z]\\d{3}-\\d{3}-\\d{3}$");
    private static final Pattern VALID_CODE_NUMBER_REGEX = Pattern.compile("^\\d{4}$");
    private static final Pattern VALID_CODE_REGEX = Pattern.compile("^\\d{6}$");

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final Catalog catalog;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;

    public String askForCodeAndValidateResponse(Credentials credentials) {
        return metrics.executeWithTimer(
                () -> {
                    String cardNumber = getCodeCardNumber();
                    verifyCodeCardNumber(cardNumber);

                    String codeNumber = getCodeNumber();
                    verifyCodeNumber(codeNumber);

                    String code = askUserForCode(cardNumber, codeNumber, credentials);
                    verifyCode(code);
                    return code;
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private String getCodeCardNumber() {
        return driverWrapper
                .tryFindElement(NEMID_CODE_CARD_NUMBER)
                .map(WebElement::getText)
                .map(String::trim)
                .orElseThrow(() -> new IllegalStateException("Cannot find NemId card number"));
    }

    private void verifyCodeCardNumber(String cardNumber) {
        if (valueDoesNotMatchPattern(cardNumber, VALID_CODE_CARD_NUMBER_REGEX)) {
            throw new IllegalStateException(
                    String.format("Invalid NemId code card number: \"%s\"", cardNumber));
        }
    }

    private String getCodeNumber() {
        return driverWrapper
                .tryFindElement(NEMID_CODE_CARD_CODE_NUMBER)
                .map(WebElement::getText)
                .map(String::trim)
                .orElseThrow(
                        () -> new IllegalStateException("Cannot find NemId code card code number"));
    }

    private void verifyCodeNumber(String codeNumber) {
        if (valueDoesNotMatchPattern(codeNumber, VALID_CODE_NUMBER_REGEX)) {
            throw new IllegalStateException(
                    String.format("Invalid NemId code number: \"%s\"", codeNumber));
        }
    }

    private String askUserForCode(String cardNumber, String codeNumber, Credentials credentials) {
        Field codeInfoField = CommonFields.KeyCardInfo.build(catalog, codeNumber, cardNumber);
        Field codeValueField = CommonFields.KeyCardCode.build(catalog, EXPECTED_CODE_LENGTH);

        statusUpdater.updateStatusPayload(
                credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_CARD_CODE);
        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        codeInfoField, codeValueField);

        return supplementalInfoResponse.get(CommonFields.KeyCardCode.FIELD_KEY);
    }

    private void verifyCode(String code) {
        if (valueDoesNotMatchPattern(code, VALID_CODE_REGEX)) {
            String errorMessage =
                    NemIdConstants.NEM_ID_PREFIX + "Invalid code card code format:" + code;
            throw NemIdError.INVALID_CODE_CARD_CODE.exception(errorMessage);
        }
    }

    private boolean valueDoesNotMatchPattern(String value, Pattern pattern) {
        return !pattern.matcher(value).matches();
    }
}
