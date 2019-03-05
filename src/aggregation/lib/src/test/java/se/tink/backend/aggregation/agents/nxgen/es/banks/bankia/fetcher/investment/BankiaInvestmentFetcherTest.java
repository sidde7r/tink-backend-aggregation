package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc.PositionWalletResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentTestConstants.ACCOUNT_TEMPLATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentTestConstants.RESPONSE_TEMPLATE;

public class BankiaInvestmentFetcherTest {

    private static String SINGLE_PAGE_ACCOUNT   = "11111111111111111111";
    private static String TWO_PAGE_ACCOUNT      = "22222222222222222222";
    private static String TOO_MANY_PAGES        = "33333333333333333333";

    private static InvestmentAccountEntity SINGLE_PAGE_ACCOUNT_ENTITY =
            SerializationUtils.deserializeFromString(
                    String.format(ACCOUNT_TEMPLATE, SINGLE_PAGE_ACCOUNT, SINGLE_PAGE_ACCOUNT),
                    InvestmentAccountEntity.class);

    private static InvestmentAccountEntity TWO_PAGE_ACCOUNT_ENTITY =
            SerializationUtils.deserializeFromString(
                    String.format(ACCOUNT_TEMPLATE, TWO_PAGE_ACCOUNT, TWO_PAGE_ACCOUNT),
                    InvestmentAccountEntity.class);

    private static InvestmentAccountEntity TOO_MANY_PAGES_ENTITIY =
            SerializationUtils.deserializeFromString(
                    String.format(ACCOUNT_TEMPLATE, TOO_MANY_PAGES, TOO_MANY_PAGES),
                    InvestmentAccountEntity.class);

    private static PositionWalletResponse EMPTY_ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(RESPONSE_TEMPLATE, "", "false", ""),
                    PositionWalletResponse.class);

    private static PositionWalletResponse TWO_PAGE_ACCOUNT_P1_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(RESPONSE_TEMPLATE,
                            "2",
                            "true",
                            BankiaInvestmentTestConstants.QUALIFICATION_JSON
                                    + ","
                                    + BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    private static PositionWalletResponse TWO_PAGE_ACCOUNT_P2_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(RESPONSE_TEMPLATE, "3", "false", BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    private static PositionWalletResponse TOO_MANY_PAGES_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(RESPONSE_TEMPLATE, "A", "true", BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    private static class MockBankiaApiClient extends BankiaApiClient {
        MockBankiaApiClient() {
            super(null, null);
        }

        int callCount = 0;

        @Override
        public PositionWalletResponse getPositionsWallet(String account, String resumePoint) {
            callCount++;
            if (account.equals(SINGLE_PAGE_ACCOUNT)) {
                return EMPTY_ACCOUNT_RESPONSE;
            } else if (account.equals(TWO_PAGE_ACCOUNT)) {
                if (resumePoint.equals("")) {
                    return TWO_PAGE_ACCOUNT_P1_RESPONSE;
                } else if (resumePoint.equals("2")) {
                    return TWO_PAGE_ACCOUNT_P2_RESPONSE;
                }
            } else if (account.equals(TOO_MANY_PAGES)) {
                return TOO_MANY_PAGES_RESPONSE;
            }

            return null;
        }
    }

    @Test
    public void fetchInvestmentAccountSinglePageOfInstruments() {
        MockBankiaApiClient mockApiClient = new MockBankiaApiClient();
        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(SINGLE_PAGE_ACCOUNT_ENTITY);

        assertNotNull(account);
        assertEquals(0, account.getPortfolios().get(0).getInstruments().size());
        assertEquals(1, mockApiClient.callCount);
    }

    @Test
    public void fetchInvestmentAccountTwoPagesOfInstruments() {
        MockBankiaApiClient mockApiClient = new MockBankiaApiClient();
        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(TWO_PAGE_ACCOUNT_ENTITY);

        assertNotNull(account);
        assertEquals(3, account.getPortfolios().get(0).getInstruments().size());
        assertEquals(2, mockApiClient.callCount);
    }


    @Test
    public void fetchInvestmentAccountTooManyPages() {
        MockBankiaApiClient mockApiClient = new MockBankiaApiClient();
        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(TOO_MANY_PAGES_ENTITIY);

        assertNotNull(account);
        assertEquals(5, account.getPortfolios().get(0).getInstruments().size());
        assertEquals(5, mockApiClient.callCount);
    }

}