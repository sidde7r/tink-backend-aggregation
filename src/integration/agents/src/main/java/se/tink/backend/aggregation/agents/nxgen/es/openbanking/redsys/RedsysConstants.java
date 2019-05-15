package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class RedsysConstants {

    public static final String INTEGRATION_NAME = "redsys";

    private RedsysConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String OAUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String REFRESH = "/token";
        public static final String CONSENTS = "/v1/consents";
        public static final String CONSENT_STATUS = "/v1/consents/%s/status";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consentId";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "AIS";
        public static final String CODE_CHALLENGE_METHOD = "plain";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
    }

    public static class HeaderKeys {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    }

    public static class HeaderValues {
        public static final String TRUE = "true";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ASPSP = "aspsp";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final boolean TRUE = true;
        public static final boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String VALID_UNTIL = "9999-12-31";
        public static final String ALL_ACCOUNTS = "allAccounts";
    }

    public static class LogTags {}

    public static class Signature {
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String KEY_ALGORITHM = "RSA";
        public static final String KEY_ID_FORMAT = "SN=%d,CA=%s";
        public static final String FORMAT =
                "keyId=\"%s\",algorithm=\"SHA-256\",headers=\"%s\",signature=\"%s\"";
    }

    public static class Links {
        public static final String SCA_REDIRECT = "scaRedirect";
        public static final String SCA_STATUS = "scaStatus";
        public static final String REDIRECT = "redirect";
        public static final String STATUS = "status";
        public static final String SELF = "self";
        public static final String OAUTH = "oAuth";
        public static final String BALANCES = "balances";
        public static final String TRANSACTIONS = "transactions";
        public static final String VIEW_ACCOUNT = "viewAccount";
        public static final String VIEW_BALANCES = "viewBalances";
        public static final String VIEW_TRANSACTIONS = "viewTransactions";
        public static final String FIRST = "first";
        public static final String NEXT = "next";
        public static final String PREVIOUS = "previous";
        public static final String LAST = "last";
        public static final String DOWNLOAD = "download";
    }

    public enum ConsentStatus {
        @JsonProperty("received")
        RECEIVED,
        @JsonProperty("rejected")
        REJECTED,
        @JsonProperty("valid")
        VALID,
        @JsonProperty("expired")
        EXPIRED,
        @JsonProperty("revokedByPsu")
        REVOKED_BY_PSU,
        @JsonProperty("terminatedByTpp")
        TERMINATED_BY_TPP;

        public boolean isReceived() {
            return this == RECEIVED;
        }

        public boolean isValid() {
            return this == VALID;
        }
    }
}
