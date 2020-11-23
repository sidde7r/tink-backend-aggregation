package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;

public class UkOpenBankingV31PaymentConstants implements UkOpenBankingPaymentConstants {

    public static final GenericTypeMapper<String, AccountIdentifier.Type>
            PAYMENT_SCHEME_TYPE_MAPPER =
                    GenericTypeMapper.<String, AccountIdentifier.Type>genericBuilder()
                            .put("UK.OBIE.Paym", AccountIdentifier.Type.PAYM_PHONE_NUMBER)
                            .put("UK.OBIE.PAN", AccountIdentifier.Type.PAYMENT_CARD_NUMBER)
                            .put("UK.OBIE.SortCodeAccountNumber", AccountIdentifier.Type.SORT_CODE)
                            .put("UK.OBIE.IBAN", AccountIdentifier.Type.IBAN)
                            .build();
    public static final ImmutableList<String> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM =
            ImmutableList.<String>builder()
                    .add(SIGNING_ALGORITHM.PS256.toString())
                    .add(SIGNING_ALGORITHM.RS256.toString())
                    .build();
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

    private UkOpenBankingV31PaymentConstants() {}

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

    public enum SIGNING_ALGORITHM {
        RS256,
        PS256
    }

    public static class Storage {

        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "paymentId";

        private Storage() {}
    }

    public static class Step {

        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String SUFFICIENT_FUNDS = "SUFFICIENT_FUNDS";
        public static final String EXECUTE_PAYMENT = "EXECUTE_PAYMENT";

        private Step() {}
    }

    public static class PaymentStatusCode {

        public static final String AWAITING_AUTHORISATION = "AwaitingAuthorisation";

        private PaymentStatusCode() {}
    }

    public static class FormValues {

        public static final String PAYMENT_CREDITOR_DEFAULT_NAME = "Payment Receiver";

        private FormValues() {}
    }

    public static class Errors {
        public static final String ACCESS_DENIED = "access_denied";
        public static final String LOGIN_REQUIRED = "login_required";
    }
}
