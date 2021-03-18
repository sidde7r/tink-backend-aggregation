package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.agent;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.CredentialKeys;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BelfiusAgentPaymentTest {
    private enum Arg implements ArgumentManagerEnum {
        IBAN,
        CREDITORS_NAME,
        CREDITORS_IBAN,
        DEBTORS_IBAN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Test
    public void testPayments() throws Exception {
        manager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("be", "be-belfius-oauth2")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .setFinancialInstitutionId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(Arg.CREDITORS_IBAN).when(creditor).getAccountNumber();
            doReturn(Arg.CREDITORS_NAME).when(creditor).getName();

            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setValue("Message");

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Arg.DEBTORS_IBAN).when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withUniqueId(UUID.randomUUID().toString().substring(5))
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withRemittanceInformation(remittanceInformation)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
