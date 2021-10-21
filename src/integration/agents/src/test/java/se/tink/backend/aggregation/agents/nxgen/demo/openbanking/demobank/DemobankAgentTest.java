package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankAgentTest {
    private static final String USERNAME = "u0001";
    private static final String PASSWORD = "abc123";

    @Test
    public void testRefresh() throws Exception {
        prepareAgentBuilder("uk", "uk-demobank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .setRefreshableItems(getRefreshableItems())
                .build()
                .testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        final String PROD_SE_USER_SSN = "195807043367";

        prepareAgentBuilder("se", "se-demobank-open-banking-bankid")
                .addCredentialField(Field.Key.USERNAME, PROD_SE_USER_SSN)
                .build()
                .testTinkLinkPayment(createMockedSePayment());
    }

    private AgentIntegrationTest.Builder prepareAgentBuilder(String market, String providerName) {
        return new AgentIntegrationTest.Builder(market, providerName)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false);
    }

    private Set<RefreshableItem> getRefreshableItems() {
        Set<RefreshableItem> items = new HashSet<>();
        items.addAll(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        items.add(RefreshableItem.IDENTITY_DATA);
        return items;
    }

    private Payment createMockedSePayment() {
        final String DEBTOR_ACCOUNT_NUMBER = "19533697";
        final String CREDITOR_ACCOUNT_NUMBER = "45783748";

        AccountIdentifier creditorAccount =
                AccountIdentifier.create(AccountIdentifierType.SE_PG, CREDITOR_ACCOUNT_NUMBER);
        Creditor creditor = new Creditor(creditorAccount, "Joe Dohn");

        AccountIdentifier debtorAccount =
                AccountIdentifier.create(AccountIdentifierType.SE_PG, DEBTOR_ACCOUNT_NUMBER);
        Debtor debtor = new Debtor(debtorAccount);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("PIS");
        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(new ExactCurrencyAmount(new BigDecimal(12), "SEK"))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                .build();
    }
}
