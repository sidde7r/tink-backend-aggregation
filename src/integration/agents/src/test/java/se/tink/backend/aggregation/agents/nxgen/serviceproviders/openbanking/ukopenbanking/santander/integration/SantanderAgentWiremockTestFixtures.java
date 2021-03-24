package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SantanderAgentWiremockTestFixtures {

    static final String PROVIDER_NAME = "uk-santander-oauth2";
    static final String AUTH_CODE = "DUMMY_AUTH_CODE";
    static final String STATE = "00000000-0000-4000-0000-000000000000";
    private static final String DESTINATION_IDENTIFIER = "77788812345678";
    private static final String CURRENCY = "GBP";
    private static final LocalDate EXECUTION_DATE_TODAY = LocalDate.now();
    private static final LocalDate EXECUTION_DATE_FAR_FUTURE = LocalDate.of(2120, 11, 2);
    private static final String UNIQUE_ID = "b900555d03124056b54930e1c53c9cac";
    private static final String REMITTANCE_INFO_VALUE = "UK Demo";
    private static final String AMOUNT = "1.00";
    private static final String CREDITOR_NAME = "Dummy creditor name";

    static Payment createDomesticPayment() {
        return createDomesticPayment(EXECUTION_DATE_TODAY);
    }

    static Payment createFarFutureDomesticPayment() {
        return createDomesticPayment(EXECUTION_DATE_FAR_FUTURE);
    }

    private static Payment createDomesticPayment(LocalDate localDate) {
        return new Payment.Builder()
                .withCreditor(createCreditor())
                .withExactCurrencyAmount(createExactCurrencyAmount())
                .withExecutionDate(localDate)
                .withCurrency(CURRENCY)
                .withRemittanceInformation(createReferenceRemittanceInformation())
                .withUniqueId(UNIQUE_ID)
                .build();
    }

    private static Creditor createCreditor() {
        return new Creditor(
                AccountIdentifier.create(AccountIdentifierType.SORT_CODE, DESTINATION_IDENTIFIER),
                CREDITOR_NAME);
    }

    private static ExactCurrencyAmount createExactCurrencyAmount() {
        return ExactCurrencyAmount.of(AMOUNT, CURRENCY);
    }

    private static RemittanceInformation createReferenceRemittanceInformation() {
        final RemittanceInformation result = new RemittanceInformation();

        result.setType(RemittanceInformationType.REFERENCE);
        result.setValue(REMITTANCE_INFO_VALUE);

        return result;
    }
}
