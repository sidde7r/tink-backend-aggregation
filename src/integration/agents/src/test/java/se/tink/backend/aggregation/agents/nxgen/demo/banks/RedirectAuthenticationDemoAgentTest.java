package se.tink.backend.aggregation.agents.nxgen.demo.banks;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class RedirectAuthenticationDemoAgentTest {
    private final String SOURCE_IDENTIFIER = "1234567891234";
    private final String DESTINATION_IDENTIFIER = "1434567891234";
    private final String SOURCE_ACCOUNT_NAME = "ha";

    private final String ACCOUNT_NAME = "IT60X0542811101551254321800";

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();
        transfer.setType(TransferType.BANK_TRANSFER);
        AccountIdentifier sourceAccount =
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, SOURCE_IDENTIFIER);
        sourceAccount.setName(SOURCE_ACCOUNT_NAME);
        transfer.setSource(sourceAccount);
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, DESTINATION_IDENTIFIER));

        // todo: Should fix this to use ExactCurrencyAmount instead of Amount
        //        BigDecimal d = new BigDecimal(1);
        //        ExactCurrencyAmount amount = new ExactCurrencyAmount(d, "GBP");
        //        LocalDate executionDate = LocalDate.now();
        //        String currency = "GBP";

        transfer.setAmount(Amount.valueOf("GBP", 1050, 2));
        transfer.setSourceMessage("TRANSFER, test Tink!");

        new AgentIntegrationTest.Builder("uk", "uk-test-open-banking-redirect")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("dummy")
                .setAppId("dummy")
                .build()
                .testBankTransferUK(transfer, false);
    }


    @Test
    public void testPaymentIT() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("it", "it-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPaymentItalia(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.IT).toString()).when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn("01551254321800").when(debtor).getAccountNumber();

            Amount amount = Amount.inEUR(1.0);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
