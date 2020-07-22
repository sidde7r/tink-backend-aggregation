package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.CARDS_LIST;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.CARD_TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.CREDIT_CARD_LIST;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.MORTGAGE_LOAN_WITH_CO_APPLICANT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.PENSION_ENGAGEMENTS;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.PENSION_OVERVIEW;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual.LansforsakringarTestData.UPCOMING_TRANSACTIONS;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeInsuranceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchUpcomingResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LansforsakringarEntitiesDeserializationTest {

    @Test
    public void testTransactionParsing() {
        FetchTransactionResponse ftr =
                SerializationUtils.deserializeFromString(
                        TRANSACTIONS, FetchTransactionResponse.class);
        Assert.assertTrue(ftr.getResponse().hasMore());
        Assert.assertNotNull(ftr.getResponse().getTransactions());
        Assert.assertEquals(
                ftr.getResponse().getTransactions().get(0).getTransactionDate(), "2020-02-10");
    }

    @Test
    public void testAccountParsing() {
        FetchAccountsResponse lar =
                SerializationUtils.deserializeFromString(ACCOUNTS, FetchAccountsResponse.class);
        Assert.assertNotNull(lar.getMainAndCoAccounts());
        Assert.assertEquals(lar.getMainAndCoAccounts().get(0).getAccountNumber(), "123456");
    }

    @Test
    public void testUpcomingTransactionParsing() {
        FetchUpcomingResponse fur =
                SerializationUtils.deserializeFromString(
                        UPCOMING_TRANSACTIONS, FetchUpcomingResponse.class);
        Assert.assertNotNull(fur.getUpcomingTransactions());
        Assert.assertEquals(fur.getUpcomingTransactions().get(0).getDate(), "2020-02-28");
    }

    @Test
    public void testListCardsParsing() {
        FetchCreditCardResponse fccr =
                SerializationUtils.deserializeFromString(CARDS_LIST, FetchCreditCardResponse.class);
        Assert.assertNotNull(fccr.getCards());
    }

    @Test
    public void testPensionOverviewParsing() {
        FetchPensionResponse fpr =
                SerializationUtils.deserializeFromString(
                        PENSION_OVERVIEW, FetchPensionResponse.class);
        Assert.assertNotNull(fpr.getLivPensionsResponseModel());
        Assert.assertFalse(fpr.getLivPensionsResponseModel().getOccupationalPensions().isEmpty());
    }

    @Test
    public void testPensionEngagementsParsing() {
        FetchPensionWithLifeInsuranceResponse fpwli =
                SerializationUtils.deserializeFromString(
                        PENSION_ENGAGEMENTS, FetchPensionWithLifeInsuranceResponse.class);
        Assert.assertNotNull(fpwli.getResponse());
        Assert.assertNotNull(fpwli.getResponse().getEngagements());
        Assert.assertFalse(fpwli.getResponse().getEngagements().isEmpty());
    }

    @Test
    public void testMortgaeLoanParsing() {
        FetchLoanDetailsResponse fldr =
                SerializationUtils.deserializeFromString(
                        MORTGAGE_LOAN_WITH_CO_APPLICANT, FetchLoanDetailsResponse.class);
        LoanAccount la = fldr.toTinkLoanAccount();
        Assert.assertTrue(la.isUniqueIdentifierEqual("1.2.3.4.5"));
        Assert.assertTrue(la.getDetails().hasCoApplicant());
        Assert.assertEquals(la.getExactBalance().getExactValue(), BigDecimal.valueOf(-250000.0));
    }

    @Test
    public void testCreditCardAccount() {
        FetchCreditCardResponse fccr =
                SerializationUtils.deserializeFromString(
                        CREDIT_CARD_LIST, FetchCreditCardResponse.class);
        Assert.assertEquals(
                "6226",
                fccr.getCards().stream()
                        .filter(CardsEntity::isCredit)
                        .collect(Collectors.toList())
                        .get(0)
                        .toTinkAccount()
                        .getIdModule()
                        .getAccountNumber());
    }

    @Test
    public void testCreditCardTransaction() {
        FetchCardTransactionsResponse fctr =
                SerializationUtils.deserializeFromString(
                        CARD_TRANSACTIONS, FetchCardTransactionsResponse.class);
        Transaction first = fctr.getTinkTransactions().stream().findFirst().get();
        Assert.assertEquals(BigDecimal.valueOf(-1023.0), first.getExactAmount().getExactValue());
    }
}
