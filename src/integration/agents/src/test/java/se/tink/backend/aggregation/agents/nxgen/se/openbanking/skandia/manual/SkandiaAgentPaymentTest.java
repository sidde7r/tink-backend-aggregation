package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.manual;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

public class SkandiaAgentPaymentTest {

    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    @Test
    public void testPayments() throws Exception {
        manager.before();
        toFromManager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "se-skandiabanken-ob")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("skandiabanken")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment());
    }

    private List<Payment> createListMockedDomesticPayment() {

        List<Payment> list = new ArrayList<>();

        LocalDate executionDate = LocalDate.now().plusDays(4);
        String currency = "SEK";
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("test");

        list.add(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE,
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
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.00))
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(remittanceInformation)
                        .build());

        return list;
    }
}
