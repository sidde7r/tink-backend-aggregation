package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import com.google.common.collect.ImmutableList;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.TokenEndpointAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;

public class UkOpenBankingV31Constants implements UkOpenBankingConstants {

    static final String TINK_UK_OPEN_BANKING_ORG_ID = "00158000016i44IAAQ";
    static final String UKOB_TAN = "openbanking.org.uk";
    static final String MONZO_ORG_ID = "001580000103U9RAAU";
    static final String DANSKEBANK_ORG_ID = "0015800000jf7AeAAI";
    static final String HSBC_ORG_ID = "00158000016i44JAAQ";
    static final String NATIONWIDE_ORG_ID = "0015800000jf8aKAAQ";
    static final String RBS_ORG_ID = "0015800000jfwB4AAI";
    static final String ULSTER_ORG_ID = "0015800000jfxrpAAA";
    static final String NATWEST_ORG_ID = "0015800000jfwxXAAQ";
    static final String BARCLAYS_ORG_ID = "0015800000jfAW1AAM";
    static final String RFC_2253_DN =
            "CN=00158000016i44IAAQ, OID.2.5.4.97=PSDSE-FINA-44059, O=Tink AB, C=GB";
    static final String GENERAL_STANDARD_ISS = "1f1YEdOMw6AphlVC6k2JQR";

    /*.jks file containing root UKOB issuing CA obtained from https://openbanking.atlassian.net/wiki/spaces/DZ/pages/80544075/OB+Root+and+Issuing+Certificates+for+Production*/
    static final byte[] UKOB_ROOT_CA_JKS =
            Base64.getDecoder()
                    .decode(
                            "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");
    static final String UKOB_ROOT_CA_JKS_PASSWORD = "tinktink";

    public static final GenericTypeMapper<String, AccountIdentifier.Type>
            PAYMENT_SCHEME_TYPE_MAPPER =
                    GenericTypeMapper.<String, AccountIdentifier.Type>genericBuilder()
                            .put("UK.OBIE.Paym", AccountIdentifier.Type.PAYM_PHONE_NUMBER)
                            .put("UK.OBIE.PAN", AccountIdentifier.Type.PAYMENT_CARD_NUMBER)
                            .put("UK.OBIE.SortCodeAccountNumber", AccountIdentifier.Type.SORT_CODE)
                            .put("UK.OBIE.IBAN", AccountIdentifier.Type.IBAN)
                            .build();

    private static final TypeMapper<PaymentStatus> paymentStatusMapper =
            TypeMapper.<PaymentStatus>builder()
                    .put(
                            PaymentStatus.PENDING,
                            "Consumed",
                            "Authorised",
                            "Pending",
                            "AcceptedSettlementInProcess")
                    .put(PaymentStatus.CREATED, "AwaitingAuthorisation")
                    .put(PaymentStatus.REJECTED, "Rejected")
                    .put(
                            PaymentStatus.PAID,
                            "AcceptedSettlementCompleted",
                            "AcceptedCreditSettlementCompleted")
                    .build();

    private static final TypeMapper<PaymentStatus> SCHEDULED_PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "InitiationPending")
                    .put(PaymentStatus.SIGNED, "InitiationCompleted")
                    .put(PaymentStatus.REJECTED, "InitiationFailed")
                    .put(PaymentStatus.CANCELLED, "Cancelled")
                    .build();

    private UkOpenBankingV31Constants() {}

    public static PaymentStatus toPaymentStatus(String consentStatus) {

        return paymentStatusMapper
                .translate(consentStatus)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format("%s unknown paymentstatus!", consentStatus)));
    }

    public static PaymentStatus scheduledPaymentStatusToPaymentStatus(
            String scheduledPaymentStatus) {
        return SCHEDULED_PAYMENT_STATUS_MAPPER
                .translate(scheduledPaymentStatus)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "%s unknown payment status!",
                                                scheduledPaymentStatus)));
    }

    public static AccountIdentifier toAccountIdentifier(String schemeName, String identification) {

        switch (schemeName) {
            case "UK.OBIE.SortCodeAccountNumber":
                return new SortCodeIdentifier(identification);
            case "UK.OBIE.Paym":
                return new PaymPhoneNumberIdentifier(identification);
            case "UK.OBIE.IBAN":
                return new IbanIdentifier(identification);
            case "PAN":
                return new PaymentCardNumberIdentifier(identification);

            default:
                throw new IllegalStateException(
                        String.format(
                                "%s unknown schemeName, identification: %s!",
                                schemeName, identification));
        }
    }

    public static final class ApiServices extends UkOpenBankingConstants.ApiServices {
        public static final String CONSENT_REQUEST = "/account-access-consents";
    }

    public static class Links {

        private Links() {}

        public static final String NEXT = "Next";
    }

    public static class Storage {

        private Storage() {}

        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class Step {

        private Step() {}

        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String SUFFICIENT_FUNDS = "SUFFICIENT_FUNDS";
        public static final String EXECUTE_PAYMENT = "EXECUTE_PAYMENT";
    }

    public static class PaymentStatusCode {

        private PaymentStatusCode() {}

        public static final String AWAITING_AUTHORISATION = "AwaitingAuthorisation";
    }

    public static class FormValues {

        private FormValues() {}

        public static final String PAYMENT_CREDITOR_DEFAULT_NAME = "Payment Receiver";
    }

    public static class Scopes {
        public static final String OPEN_ID = "openid";
        public static final String ACCOUNTS = "accounts";
        public static final String PAYMENTS = "payments";
    }

    public enum SIGNING_ALGORITHM {
        RS256,
        PS256
    }

    public static final ImmutableList<String> MANDATORY_RESPONSE_TYPES =
            ImmutableList.<String>builder().add("code").add(CallbackParams.ID_TOKEN).build();

    public static final ImmutableList<String> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM =
            ImmutableList.<String>builder()
                    .add(SIGNING_ALGORITHM.PS256.toString())
                    .add(SIGNING_ALGORITHM.RS256.toString())
                    .build();

    static final ImmutableList<TokenEndpointAuthMethod> PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS =
            ImmutableList.<TokenEndpointAuthMethod>builder()
                    .add(TokenEndpointAuthMethod.CLIENT_SECRET_POST)
                    .add(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                    .add(TokenEndpointAuthMethod.PRIVATE_KEY_JWT)
                    .add(TokenEndpointAuthMethod.TLS_CLIENT_AUTH)
                    .build();

    public static class Params {
        public static final String CLIENT_ID = "client_id";
        public static final String SOFTWARE_ID = "software_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String NONCE = "nonce";
        public static final String SOFTWARE_JWKS_ENDPOINT = "software_jwks_endpoint";
    }

    public static final String CLIENT_ASSERTION_TYPE =
            "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static class CallbackParams {
        public static final String CODE = "code";
        static final String ID_TOKEN = "id_token";
        public static final String ERROR = "error";
        static final String ERROR_DESCRIPTION = "error_description";
    }

    public static class PersistentStorageKeys {
        public static final String AIS_ACCESS_TOKEN = "open_id_ais_access_token";
        static final String AIS_ACCOUNT_PERMISSIONS_GRANTED = "ais_account_permissions_granted";
        public static final String LAST_SCA_TIME = "last_SCA_time";
    }

    public static class Errors {
        public static final String ACCESS_DENIED = "access_denied";
        public static final String LOGIN_REQUIRED = "login_required";
        static final String SERVER_ERROR = "server_error";
        static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
        public static final String PSU_CANCELLED = "PSU Cancelled";
    }

    public static class Ps256 {
        public static class PayloadClaims {
            public static final String ISSUER = "iss";
            public static final String AUDIENCE = "aud";
        }
    }
}
