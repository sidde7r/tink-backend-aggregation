package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_BANK_ID_BLOCKED_1;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_BANK_ID_BLOCKED_2;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_BANK_ID_SUBSCRIPTION_CHANGED;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_BANK_ID_TIMEOUT_1;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_BANK_ID_TIMEOUT_2;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_OPERATOR_ERROR_RETRY_1;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.MOBILE_OPERATOR_ERROR_RETRY_2;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.ErrorCode.WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE_ERROR_CODE;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OidcSessionHelper {

    static String extractContinueOidcAuthUrl(HttpResponse response) {
        Document responseDocument = Jsoup.parse(response.getBody(String.class));

        return responseDocument.getElementsByTag("meta").stream()
                .filter(tag -> Objects.equals(tag.attr("name"), "cookie-activation-authUrl"))
                .map(tag -> tag.attr("content"))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not find authentication url in oidc activation response page"));
    }

    static OidcSessionDetails extractBankIdSessionDetails(HttpResponse bankIdResponse) {
        Document oidcSessionInfo = Jsoup.parse(bankIdResponse.getBody(String.class));
        Map<String, String> metaElements =
                oidcSessionInfo.getElementsByTag("meta").stream()
                        .filter(x -> x.attr("name") != null && x.attr("name").startsWith("oidc-"))
                        .collect(Collectors.toMap(x -> x.attr("name"), z -> z.attr("content")));

        return new OidcSessionDetails(
                metaElements.get("oidc-action"),
                metaElements.get("oidc-error"),
                metaElements.get("oidc-errorCode"),
                metaElements.get("oidc-errorMessage"),
                metaElements.get("oidc-merchantReference"),
                metaElements.get("oidc-sid"));
    }

    static void throwBankIdError(String errorCode) {
        switch (errorCode) {
            case WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE_ERROR_CODE:
                throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception();
            case MOBILE_OPERATOR_ERROR_RETRY_1:
            case MOBILE_OPERATOR_ERROR_RETRY_2:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error Code C131. This error indicates that your mobile operator has trouble or a process is"
                                        + " running on your phone number. Restart your phone and try again in 5 minutes."));
            case MOBILE_BANK_ID_BLOCKED_1:
            case MOBILE_BANK_ID_BLOCKED_2:
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        new LocalizableKey(
                                "Error Code C30F. " + BankIdError.BLOCKED.userMessage().get()));
            case MOBILE_BANK_ID_TIMEOUT_1:
            case MOBILE_BANK_ID_TIMEOUT_2:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error code C308. There was a timeout due to slow response time. This can happen if there"
                                        + " are weak or unstable signals or if the mobile operator is having trouble."
                                        + " Please try again in 5 minutes."));
            case MOBILE_BANK_ID_SUBSCRIPTION_CHANGED:
                throw BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE.exception();
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        "Unexpected error code from Bank ID: " + errorCode);
        }
    }

    static String getBidCodeOrThrowIfNotPresent(HttpResponse oidcSessionResponse) {
        String bidCode = extractBidCode(oidcSessionResponse);
        if (bidCode == null) {
            String errorCode = getErrorCode(oidcSessionResponse);
            throwBidCodeException(errorCode);
        }
        return bidCode;
    }

    private static String extractBidCode(HttpResponse bidCodeResponse) {
        List<URI> redirects = bidCodeResponse.getRedirects();
        if (!redirects.isEmpty()) {
            URI uri = redirects.get(redirects.size() - 1);
            return extractBidCode(uri);
        }
        return null;
    }

    private static String extractBidCode(URI uri) {
        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
        return params.stream()
                .filter(x -> "code".equalsIgnoreCase(x.getName()))
                .findFirst()
                .map(NameValuePair::getValue)
                .orElse(null);
    }

    private static String getErrorCode(HttpResponse oidcSessionResponse) {
        return extractBankIdSessionDetails(oidcSessionResponse).getErrorCode();
    }

    private static void throwBidCodeException(String errorCode) {
        if (errorCode != null) {
            throwBankIdError(errorCode);
        } else {
            throw new LoginException(LoginError.DEFAULT_MESSAGE, "Couldn't determine BID code");
        }
    }
}
