package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;

public class UkOpenBankingV31Constants extends UkOpenBankingConstants {

    public static final int HEX_SIZE = 8;

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .ignoreKeys("ChargeCard", "EMoney", "PrePaidCard")
                    .build();

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
                    .put(PaymentStatus.PAID, "AcceptedSettlementCompleted")
                    .build();

    public static PaymentStatus toPaymentStatus(String consentStatus) {

        return paymentStatusMapper
                .translate(consentStatus)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format("%s unknown paymentstatus!", consentStatus)));
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
        public static final String AISP_PREFIX = "/aisp";
        public static final String PISP_PREFIX = "/pisp";
    }

    public static class Links {
        public static final String NEXT = "Next";
    }

    public static class Storage {
        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class Step {
        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String SUFFICIENT_FUNDS = "SUFFICIENT_FUNDS";
        public static final String EXECUTE_PAYMENT = "EXECUTE_PAYMENT";
    }
}
