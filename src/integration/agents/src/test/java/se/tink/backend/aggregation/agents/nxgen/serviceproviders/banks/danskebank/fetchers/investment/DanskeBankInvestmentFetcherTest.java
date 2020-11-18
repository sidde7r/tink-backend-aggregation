package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.InvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecuritiesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecurityDetailsResponse;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankInvestmentFetcherTest {

    private static final String INVESTMENT_ENTITIES_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources";

    private static final String ACCOUNT_IDENTIFIER_FIRST = "D123456789";
    private static final String ACCOUNT_NUMBER_FIRST = "123456789";
    private static final String ACCOUNT_NAME_FIRST = "Opbevaringsdepot";
    private static final String RAW_TYPE_FIRST = "CustodyAccount";

    private static final String ACCOUNT_IDENTIFIER_SECOND = "DU123PA1234PAP";
    private static final String ACCOUNT_NUMBER_SECOND = "1234554321";
    private static final String ACCOUNT_NAME_SECOND = "June";
    private static final String RAW_TYPE_SECOND = "InvestmentAgreement";

    private DanskeBankApiClient apiClient;
    private DanskeBankInvestmentFetcher danskeBankInvestmentFetcher;

    private InvestmentAccountsResponse investmentAccountsResponse;

    private ListSecuritiesResponse firstListSecuritiesResponse;
    private ListSecuritiesResponse secondListSecuritiesResponse;

    private ListSecurityDetailsResponse firstListSecurityDetailsResponse;
    private ListSecurityDetailsResponse secondListSecurityDetailsResponse;

    @Before
    public void setup() {
        apiClient = mock(DanskeBankApiClient.class);
        DanskeBankConfiguration configuration = mock(DanskeBankConfiguration.class);

        when(configuration.getMarketCode()).thenReturn(DanskeBankConstants.Market.DK_MARKET);

        danskeBankInvestmentFetcher = new DanskeBankInvestmentFetcher(apiClient, configuration);

        investmentAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(INVESTMENT_ENTITIES_FILE_PATH, "investmentAccounts.json")
                                .toFile(),
                        InvestmentAccountsResponse.class);

        firstListSecuritiesResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(INVESTMENT_ENTITIES_FILE_PATH, "investmentSecuritiesOne.json")
                                .toFile(),
                        ListSecuritiesResponse.class);

        secondListSecuritiesResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(INVESTMENT_ENTITIES_FILE_PATH, "investmentSecuritiesTwo.json")
                                .toFile(),
                        ListSecuritiesResponse.class);

        firstListSecurityDetailsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(INVESTMENT_ENTITIES_FILE_PATH, "securityDetailsOne.json")
                                .toFile(),
                        ListSecurityDetailsResponse.class);

        secondListSecurityDetailsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(INVESTMENT_ENTITIES_FILE_PATH, "securityDetailsTwo.json")
                                .toFile(),
                        ListSecurityDetailsResponse.class);
    }

    @Test
    public void shouldFetchAccountsWhenGroupAccountIsInResponse() {
        // given
        when(apiClient.listCustodyAccounts()).thenReturn(investmentAccountsResponse);
        when(apiClient.listSecurities(any()))
                .thenReturn(firstListSecuritiesResponse, secondListSecuritiesResponse);
        when(apiClient.listSecurityDetails(any()))
                .thenReturn(firstListSecurityDetailsResponse, secondListSecurityDetailsResponse);

        // when
        List<InvestmentAccount> investmentAccounts =
                new ArrayList<>(danskeBankInvestmentFetcher.fetchAccounts());
        InvestmentAccount expected =
                getInvestmentAccount(
                        ACCOUNT_IDENTIFIER_FIRST,
                        ACCOUNT_NUMBER_FIRST,
                        ACCOUNT_NAME_FIRST,
                        RAW_TYPE_FIRST,
                        getFirstInvestmentPortfolios());
        InvestmentAccount secondExpected =
                getInvestmentAccount(
                        ACCOUNT_IDENTIFIER_SECOND,
                        ACCOUNT_NUMBER_SECOND,
                        ACCOUNT_NAME_SECOND,
                        RAW_TYPE_SECOND,
                        getSecondInvestmentPortfolios());

        // then
        assertThat(investmentAccounts.size()).isEqualTo(2);
        assertInvestmentAccount(investmentAccounts.get(0), expected);
        assertInvestmentAccount(investmentAccounts.get(1), secondExpected);
    }

    @Test
    public void shouldReturnEmptyListWhenResponseIsNull() {
        // given
        when(apiClient.listCustodyAccounts()).thenReturn(new InvestmentAccountsResponse());

        // when
        List<InvestmentAccount> investmentAccounts =
                new ArrayList<>(danskeBankInvestmentFetcher.fetchAccounts());

        // then
        assertThat(investmentAccounts).isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldFetchAccountsWhenListSecuritiesResponseIsEmpty() {
        // given
        ListSecuritiesResponse emptyListSecuritiesResponse = new ListSecuritiesResponse();
        emptyListSecuritiesResponse.setMarketValue(BigDecimal.ZERO);
        emptyListSecuritiesResponse.setMarketValueCurrency("NOK");
        emptyListSecuritiesResponse.setPerformance(BigDecimal.ZERO);
        emptyListSecuritiesResponse.setPerformancePct(BigDecimal.ZERO);
        emptyListSecuritiesResponse.setSecurities(Collections.emptyList());
        firstListSecuritiesResponse = emptyListSecuritiesResponse;
        secondListSecuritiesResponse = emptyListSecuritiesResponse;

        when(apiClient.listCustodyAccounts()).thenReturn(investmentAccountsResponse);
        when(apiClient.listSecurities(any()))
                .thenReturn(firstListSecuritiesResponse, secondListSecuritiesResponse);

        // when
        List<InvestmentAccount> investmentAccounts =
                new ArrayList<>(danskeBankInvestmentFetcher.fetchAccounts());

        // then
        assertThat(investmentAccounts.size()).isEqualTo(2);
        assertThat(investmentAccounts.get(0).getSystemPortfolios().size()).isEqualTo(1);
        assertThat(investmentAccounts.get(1).getSystemPortfolios().size()).isEqualTo(1);
        assertThat(investmentAccounts.get(0).getSystemPortfolios().get(0).getInstruments())
                .isEqualTo(Collections.emptyList());
        assertThat(investmentAccounts.get(1).getSystemPortfolios().get(0).getInstruments())
                .isEqualTo(Collections.emptyList());
    }

    private void assertInvestmentAccount(InvestmentAccount result, InvestmentAccount expected) {
        assertThat(result)
                .isEqualToIgnoringGivenFields(
                        expected, "systemPortfolios", "capabilities", "sourceInfo");
        assertThat(result.getSystemPortfolios().size()).isEqualTo(1);
        assertThat(result.getSystemPortfolios().get(0))
                .isEqualToIgnoringGivenFields(expected.getSystemPortfolios().get(0), "instruments");
        assertThat(result.getSystemPortfolios().get(0).getInstruments()).size().isEqualTo(1);
        assertThat(result.getSystemPortfolios().get(0).getInstruments().get(0))
                .isEqualToComparingFieldByField(
                        expected.getSystemPortfolios().get(0).getInstruments().get(0));
    }

    private InvestmentAccount getInvestmentAccount(
            String identifier,
            String accountNumber,
            String name,
            String rawType,
            List<Portfolio> portfolios) {
        return InvestmentAccount.builder(identifier)
                .setCashBalance(ExactCurrencyAmount.zero("DKK"))
                .setAccountNumber(accountNumber)
                .setName(name)
                .setPortfolios(portfolios)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .sourceInfo(
                        AccountSourceInfo.builder()
                                .bankProductName(name)
                                .bankProductCode(rawType)
                                .build())
                .build();
    }

    private List<Portfolio> getFirstInvestmentPortfolios() {
        List<Portfolio> portfolios = new ArrayList<>();
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(12345.12);
        portfolio.setRawType(RAW_TYPE_FIRST);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(ACCOUNT_IDENTIFIER_FIRST);
        portfolio.setTotalProfit(8653.12);
        portfolio.setInstruments(getFirstInstruments());
        portfolios.add(portfolio);
        return portfolios;
    }

    private List<Instrument> getFirstInstruments() {
        List<Instrument> instruments = new ArrayList<>();
        Instrument instrument = new Instrument();
        instrument.setAverageAcquisitionPrice(
                BigDecimal.valueOf(1234.12)
                        .add(BigDecimal.valueOf(-123.45))
                        .divide(BigDecimal.valueOf(12345), 4, RoundingMode.HALF_UP));
        instrument.setCurrency("NOK");
        instrument.setIsin("NO0012345678");
        instrument.setMarketValue(1234.12);
        instrument.setName("Norwegian Something");
        instrument.setPrice(0.1234);
        instrument.setProfit(-123.45);
        instrument.setQuantity((double) 12345);
        instrument.setRawType("Shares");
        instrument.setType(Instrument.Type.STOCK);
        instrument.setUniqueIdentifier("NO0012345678NOK");
        instruments.add(instrument);
        return instruments;
    }

    private List<Portfolio> getSecondInvestmentPortfolios() {
        List<Portfolio> portfolios = new ArrayList<>();
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(2345.12);
        portfolio.setRawType(RAW_TYPE_SECOND);
        portfolio.setType(Portfolio.Type.OTHER);
        portfolio.setUniqueIdentifier(ACCOUNT_IDENTIFIER_SECOND);
        portfolio.setTotalProfit(234.12);
        portfolio.setInstruments(getSecondInstruments());
        portfolios.add(portfolio);
        return portfolios;
    }

    private List<Instrument> getSecondInstruments() {
        List<Instrument> instruments = new ArrayList<>();
        Instrument instrument = new Instrument();
        instrument.setAverageAcquisitionPrice(
                BigDecimal.valueOf(2349.16)
                        .add(BigDecimal.valueOf(123.12))
                        .divide(BigDecimal.valueOf(21.21211), 4, RoundingMode.HALF_UP));
        instrument.setCurrency("DKK");
        instrument.setIsin("LU123456789");
        instrument.setMarketValue(2349.16);
        instrument.setName("June OppScr Jdkk");
        instrument.setPrice(123.45);
        instrument.setProfit(123.12);
        instrument.setQuantity(21.21211);
        instrument.setRawType("Funds");
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier("LU123456789DKK");
        instruments.add(instrument);
        return instruments;
    }
}
