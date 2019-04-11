package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentTestConstants.ACCOUNT_TEMPLATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentTestConstants.RESPONSE_TEMPLATE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc.PositionWalletResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankiaInvestmentFetcherTest {

    private static String SINGLE_PAGE_ACCOUNT = "11111111111111111111";
    private static String TWO_PAGE_ACCOUNT = "22222222222222222222";
    private static String TOO_MANY_PAGES = "33333333333333333333";

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
                    String.format(
                            RESPONSE_TEMPLATE,
                            "2",
                            "true",
                            BankiaInvestmentTestConstants.QUALIFICATION_JSON
                                    + ","
                                    + BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    private static PositionWalletResponse TWO_PAGE_ACCOUNT_P2_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(
                            RESPONSE_TEMPLATE,
                            "3",
                            "false",
                            BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    private static PositionWalletResponse TOO_MANY_PAGES_RESPONSE =
            SerializationUtils.deserializeFromString(
                    String.format(
                            RESPONSE_TEMPLATE,
                            "A",
                            "true",
                            BankiaInvestmentTestConstants.QUALIFICATION_JSON),
                    PositionWalletResponse.class);

    @Test
    public void fetchInvestmentAccountSinglePageOfInstruments() {
        BankiaApiClient mockApiClient = mock(BankiaApiClient.class);
        when(mockApiClient.getPositionsWallet(SINGLE_PAGE_ACCOUNT, ""))
                .thenReturn(EMPTY_ACCOUNT_RESPONSE);

        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(SINGLE_PAGE_ACCOUNT_ENTITY);

        assertNotNull(account);
        assertEquals(0, account.getPortfolios().get(0).getInstruments().size());
        verify(mockApiClient, times(1)).getPositionsWallet(SINGLE_PAGE_ACCOUNT, "");
    }

    @Test
    public void fetchInvestmentAccountTwoPagesOfInstruments() {
        BankiaApiClient mockApiClient = mock(BankiaApiClient.class);
        when(mockApiClient.getPositionsWallet(TWO_PAGE_ACCOUNT, ""))
                .thenReturn(TWO_PAGE_ACCOUNT_P1_RESPONSE);
        when(mockApiClient.getPositionsWallet(TWO_PAGE_ACCOUNT, "2"))
                .thenReturn(TWO_PAGE_ACCOUNT_P2_RESPONSE);
        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(TWO_PAGE_ACCOUNT_ENTITY);

        assertNotNull(account);
        assertEquals(3, account.getPortfolios().get(0).getInstruments().size());
        verify(mockApiClient, times(2)).getPositionsWallet(eq(TWO_PAGE_ACCOUNT), any());
    }

    @Test
    public void fetchInvestmentAccountTooManyPages() {
        BankiaApiClient mockApiClient = mock(BankiaApiClient.class);
        when(mockApiClient.getPositionsWallet(eq(TOO_MANY_PAGES), any()))
                .thenReturn(TOO_MANY_PAGES_RESPONSE);

        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(mockApiClient);
        InvestmentAccount account = fetcher.fetchInvestmentAccount(TOO_MANY_PAGES_ENTITIY);

        assertNotNull(account);
        assertEquals(3, account.getPortfolios().get(0).getInstruments().size());
        verify(mockApiClient, times(1)).getPositionsWallet(eq(TOO_MANY_PAGES), eq(""));
        verify(mockApiClient, times(2)).getPositionsWallet(eq(TOO_MANY_PAGES), eq("A"));
    }
}
