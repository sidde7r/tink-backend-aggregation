package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
@Getter
public class ThirdPartyAppCallbackProcessor {

    private final OAuth2ThirdPartyAppRequestParamsProvider oAuth2ThirdPartyAppRequestParamsProvider;

    public String getAccessCodeFromCallbackData(Map<String, String> callbackData) {
        final String codeKey =
                oAuth2ThirdPartyAppRequestParamsProvider.getCallbackDataAuthCodeKey();
        return getCallbackElement(callbackData, codeKey)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "callbackData did not contain '%s'.", codeKey)));
    }

    public boolean isThirdPartyAppLoginSuccessful(Map<String, String> callbackData) {
        if (MapUtils.isEmpty(callbackData)) {
            throw new IllegalArgumentException("callbackData did is not present.");
        }

        final Optional<String> maybeError =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR);

        final boolean hasAuthCode =
                callbackData.containsKey(
                        oAuth2ThirdPartyAppRequestParamsProvider.getCallbackDataAuthCodeKey());
        final boolean hasError = maybeError.map(err -> processError(err, callbackData)).isPresent();
        final boolean isSuccessful = hasAuthCode && !hasError;

        if (isSuccessful) {
            log.info("OAuth2 callback success.");
        }

        return isSuccessful;
    }

    protected static Optional<String> getCallbackElement(
            Map<String, String> callbackData, String key) {
        final String value = callbackData.get(key);

        return Optional.ofNullable(value).filter(StringUtils::isNotEmpty);
    }

    protected String processError(String error, Map<String, String> callbackData) {
        final OAuth2Constants.ErrorType errorType = OAuth2Constants.ErrorType.getErrorType(error);
        if (OAuth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                || OAuth2Constants.ErrorType.LOGIN_REQUIRED.equals(errorType)) {
            log.error(
                    "OAuth2 {} callback: {}",
                    errorType.getValue(),
                    SerializationUtils.serializeToString(callbackData));
            return error;
        }

        final Optional<String> errorDescription =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION);

        throw new IllegalArgumentException(
                String.format("Unknown error: %s:%s.", errorType, errorDescription.orElse("")));
    }
}
