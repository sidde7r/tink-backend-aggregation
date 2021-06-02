package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LansforsakringarAgentPaymentTest {

    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;
    @Before
    public void setup() {
        manager.before();
        toFromManager.before();
         builder =
            new AgentIntegrationTest.Builder("SE", "se-lansforsakringar-ob")
                .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                .expectLoggedIn(false)
                .setFinancialInstitutionId("lansforsakringar")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment());
    }

    @Test
    public void testCancelPayments() throws Exception {
        builder.build().testCancelPayment("FO7GGMcPGmuH6xdQFovC0AeOHDn9sQuhXtNi732NIaSFOtrFyv1zHQ5m6OWJPiq7");
    }

    private List<Payment> createListMockedDomesticPayment() {

        List<Payment> list = new ArrayList<>();

        LocalDate executionDate = LocalDate.now().plusDays(7);
        String currency = "SEK";
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("33001227314");

        list.add(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE_BG,
                                                toFromManager.get(
                                                        ToAccountFromAccountArgumentEnum
                                                                .TO_ACCOUNT))))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE,
                                                toFromManager.get(
                                                        ToAccountFromAccountArgumentEnum
                                                                .FROM_ACCOUNT))))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(0.01))
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(remittanceInformation)
                        .build());

        return list;
    }
}
