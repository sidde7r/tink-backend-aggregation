package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InstrumentsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.PortfolioPerformanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarInvestmentsFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String INVEST_ACCOUNT_NO = "1212-0101123456";
    private static final String INVEST_ACCOUNT_NO_WITH_QUOTES = "\"1212-0101123456\"";
    private static final String CURRENCY = "DKK";
    private static final double CASH_VALUE = 1.02;
    private static final double TOTAL_PROFIT = -61.15;
    private static final double TOTAL_VALUE = 15.84;
    private static final double TOTAL_OPEN_POSITIONS_VALUE = 13.94;

    private LunarInvestmentsFetcher investmentsFetcher;
    private FetcherApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(FetcherApiClient.class);
        investmentsFetcher = new LunarInvestmentsFetcher(apiClient);
    }

    @Test
    @Parameters(method = "investmentsParams")
    public void shouldFetchInvestments(
            InvestmentsResponse investmentsResponse,
            InstrumentsResponse instrumentsResponse,
            PortfolioPerformanceResponse performanceResponse,
            List<InvestmentAccount> expected) {
        // given
        when(apiClient.fetchInvestments()).thenReturn(investmentsResponse);
        when(apiClient.fetchInstruments()).thenReturn(instrumentsResponse);
        when(apiClient.fetchPerformanceData()).thenReturn(performanceResponse);

        // when
        List<InvestmentAccount> result =
                (List<InvestmentAccount>) investmentsFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
    }

    private Object[] investmentsParams() throws IOException {
        return new Object[] {
            new Object[] {
                deserialize("invest_portfolio.json", InvestmentsResponse.class),
                deserialize("invest_instruments.json", InstrumentsResponse.class),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        CASH_VALUE,
                        TOTAL_PROFIT,
                        TOTAL_OPEN_POSITIONS_VALUE,
                        1.9,
                        buildTestInstruments())
            },
            new Object[] {
                deserialize("invest_portfolio.json", InvestmentsResponse.class),
                deserialize("invest_instruments.json", InstrumentsResponse.class),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        CASH_VALUE,
                        TOTAL_PROFIT,
                        TOTAL_OPEN_POSITIONS_VALUE,
                        1.9,
                        buildTestInstruments())
            },
            new Object[] {
                getTestInvestmentsResponse(
                        INVEST_ACCOUNT_NO_WITH_QUOTES, CASH_VALUE, null, TOTAL_VALUE),
                deserialize(
                        "invest_instruments_without_position_or_deleted.json",
                        InstrumentsResponse.class),
                deserialize(
                        "invest_performance_data_empty.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        CASH_VALUE, 0.0, TOTAL_VALUE, 0.0, Collections.emptyList())
            },
            new Object[] {
                getTestInvestmentsResponse(
                        INVEST_ACCOUNT_NO_WITH_QUOTES, CASH_VALUE, null, TOTAL_VALUE),
                new InstrumentsResponse(),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        CASH_VALUE, TOTAL_PROFIT, TOTAL_VALUE, 0.0, Collections.emptyList())
            },
            new Object[] {
                getTestInvestmentsResponse(
                        INVEST_ACCOUNT_NO_WITH_QUOTES, CASH_VALUE, null, TOTAL_VALUE),
                SerializationUtils.deserializeFromString(
                        "{\"instruments\": []}", InstrumentsResponse.class),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        CASH_VALUE, TOTAL_PROFIT, TOTAL_VALUE, 0.0, Collections.emptyList())
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, null, null, null),
                new InstrumentsResponse(),
                new PortfolioPerformanceResponse(),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, null, null, null),
                new InstrumentsResponse(),
                deserialize(
                        "invest_performance_data_empty.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, 0.0, 0.0, 0.0),
                new InstrumentsResponse(),
                deserialize(
                        "invest_performance_data_empty.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, 0.0, 0.0, 0.0),
                new InstrumentsResponse(),
                new PortfolioPerformanceResponse(),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
            new Object[] {
                getTestInvestmentsResponse(
                        INVEST_ACCOUNT_NO_WITH_QUOTES, null, TOTAL_OPEN_POSITIONS_VALUE, null),
                deserialize("invest_instruments.json", InstrumentsResponse.class),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        0.0, TOTAL_PROFIT, TOTAL_OPEN_POSITIONS_VALUE, 0.0, buildTestInstruments())
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, 2.0, 30.0, 25.0),
                deserialize("invest_instruments.json", InstrumentsResponse.class),
                deserialize("investment_performance_data.json", PortfolioPerformanceResponse.class),
                buildExpectedInvestmentAccounts(
                        2.0, TOTAL_PROFIT, 30.0, -5.0, buildTestInstruments())
            },
        };
    }

    @Test
    @Parameters(method = "investmentsResponseParams")
    public void shouldMapAndFilterInvestmentsResponseAndReturnEmptyList(
            InvestmentsResponse investmentsResponse) {
        // given
        when(apiClient.fetchInvestments()).thenReturn(investmentsResponse);

        // when
        List<InvestmentAccount> result =
                (List<InvestmentAccount>) investmentsFetcher.fetchAccounts();

        // then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    private Object[] investmentsResponseParams() throws IOException {
        return new Object[] {
            new Object[] {new InvestmentsResponse()},
            new Object[] {null},
            new Object[] {
                getTestInvestmentsResponse(
                        null, CASH_VALUE, TOTAL_OPEN_POSITIONS_VALUE, TOTAL_VALUE)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        "{\"portfolio\": {}}", InvestmentsResponse.class)
            },
        };
    }

    private List<InvestmentAccount> buildExpectedInvestmentAccounts(
            Double cashValue,
            Double totalProfit,
            Double totalValue,
            Double cashBalance,
            List<InstrumentModule> instruments) {
        return Collections.singletonList(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(
                                PortfolioModule.builder()
                                        .withType(PortfolioModule.PortfolioType.DEPOT)
                                        .withUniqueIdentifier(INVEST_ACCOUNT_NO)
                                        .withCashValue(cashValue)
                                        .withTotalProfit(totalProfit)
                                        .withTotalValue(totalValue)
                                        .withInstruments(instruments)
                                        .build())
                        .withCashBalance(ExactCurrencyAmount.of(cashBalance, CURRENCY))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(INVEST_ACCOUNT_NO)
                                        .withAccountNumber(INVEST_ACCOUNT_NO)
                                        .withAccountName(INVEST_ACCOUNT_NO)
                                        .addIdentifier(new DanishIdentifier(INVEST_ACCOUNT_NO))
                                        .build())
                        .build());
    }

    private List<InstrumentModule> buildTestInstruments() {
        return Arrays.asList(
                InstrumentModule.builder()
                        .withType(InstrumentModule.InstrumentType.STOCK)
                        .withId(
                                InstrumentIdModule.builder()
                                        .withUniqueIdentifier(
                                                "bdd9b2ff-9638-47f8-bb02-83516f2b7c81")
                                        .withName("Testing title AB")
                                        .build())
                        .withMarketPrice(0.89)
                        .withMarketValue(0.89)
                        .withAverageAcquisitionPrice(0.90)
                        .withCurrency(CURRENCY)
                        .withQuantity(15)
                        .withProfit(-0.78)
                        .setRawType("stock")
                        .setTicker("ABCD")
                        .build(),
                InstrumentModule.builder()
                        .withType(InstrumentModule.InstrumentType.OTHER)
                        .withId(
                                InstrumentIdModule.builder()
                                        .withUniqueIdentifier(
                                                "f5f9a5a3-e9eb-4904-8960-28c2fcd21c39")
                                        .withName("Another AB")
                                        .build())
                        .withMarketPrice(0.89)
                        .withMarketValue(0.89)
                        .withAverageAcquisitionPrice(0.90)
                        .withCurrency(CURRENCY)
                        .withQuantity(5)
                        .withProfit(-0.78)
                        .setRawType("other")
                        .setTicker("EFGH")
                        .build());
    }

    private InvestmentsResponse getTestInvestmentsResponse(
            String accountNumber,
            Double cashBalance,
            Double totalOpenPositionsValue,
            Double totalValue)
            throws IOException {
        String investmentsResponseString =
                String.format(
                        FileUtils.readFileToString(
                                Paths.get(TEST_DATA_PATH, "invest_portfolio_with_parameters")
                                        .toFile(),
                                StandardCharsets.UTF_8),
                        accountNumber,
                        cashBalance,
                        totalOpenPositionsValue,
                        totalValue);
        return SerializationUtils.deserializeFromString(
                investmentsResponseString, InvestmentsResponse.class);
    }

    private <T> T deserialize(String fileName, Class<T> responseClass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, fileName).toFile(), responseClass);
    }
}
