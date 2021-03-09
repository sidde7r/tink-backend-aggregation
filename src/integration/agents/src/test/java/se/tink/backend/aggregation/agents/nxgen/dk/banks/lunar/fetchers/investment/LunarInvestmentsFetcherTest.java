package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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

    private LunarInvestmentsFetcher investmentsFetcher;
    private FetcherApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(FetcherApiClient.class);
        investmentsFetcher = new LunarInvestmentsFetcher(apiClient);

        when(apiClient.fetchPerformanceData())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "investment_performance_data.json")
                                        .toFile(),
                                PortfolioPerformanceResponse.class));
    }

    @Test
    @Parameters(method = "investmentsParams")
    public void shouldFetchInvestments(
            InvestmentsResponse investmentsResponse,
            InstrumentsResponse instrumentsResponse,
            List<InvestmentAccount> expected) {
        // given
        when(apiClient.fetchInvestments()).thenReturn(investmentsResponse);
        when(apiClient.fetchInstruments()).thenReturn(instrumentsResponse);

        // when
        List<InvestmentAccount> result =
                (List<InvestmentAccount>) investmentsFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
    }

    private Object[] investmentsParams() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "invest_portfolio.json").toFile(),
                        InvestmentsResponse.class),
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "invest_instruments.json").toFile(),
                        InstrumentsResponse.class),
                buildExpectedInvestmentAccounts(25.93, -56.06, 43.73, 1.27, buildTestInstruments())
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, null, null, null),
                new InstrumentsResponse(),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
            new Object[] {
                getTestInvestmentsResponse(INVEST_ACCOUNT_NO_WITH_QUOTES, 0.0, 0.0, 0.0),
                new InstrumentsResponse(),
                buildExpectedInvestmentAccounts(0.0, 0.0, 0.0, 0.0, Collections.emptyList()),
            },
        };
    }

    @Test
    @Parameters(method = "responseExceptionParams")
    public void shouldCatchHttpResponseExceptionAndReturnEmptyList(
            boolean shouldFetchInvestmentsThrow, boolean shouldFetchPerformanceDataThrow) {
        // given
        when(apiClient.fetchInvestments())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "invest_portfolio.json").toFile(),
                                InvestmentsResponse.class));

        // and
        if (shouldFetchInvestmentsThrow) {
            when(apiClient.fetchInvestments()).thenThrow(new HttpResponseException(null, null));
        } else if (shouldFetchPerformanceDataThrow) {
            when(apiClient.fetchPerformanceData()).thenThrow(new HttpResponseException(null, null));
        } else {
            when(apiClient.fetchInstruments()).thenThrow(new HttpResponseException(null, null));
        }

        // when
        List<InvestmentAccount> result =
                (List<InvestmentAccount>) investmentsFetcher.fetchAccounts();

        // then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    private Object[] responseExceptionParams() {
        return new Object[] {
            new Object[] {true, false},
            new Object[] {false, true},
            new Object[] {false, false},
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

    private Object[] investmentsResponseParams() {
        return new Object[] {
            new Object[] {new InvestmentsResponse()},
            new Object[] {null},
            new Object[] {getTestInvestmentsResponse(null, 25.93, 18.01, 45.0)},
            new Object[] {
                SerializationUtils.deserializeFromString(
                        "{\"portfolio\": {}}", InvestmentsResponse.class)
            },
        };
    }

    @Test
    @Parameters(method = "instrumentsParams")
    public void shouldFetchInvestmentsWithoutInstruments(InstrumentsResponse instrumentsResponse) {
        // given
        when(apiClient.fetchInvestments())
                .thenReturn(
                        getTestInvestmentsResponse(
                                INVEST_ACCOUNT_NO_WITH_QUOTES, 43.94, null, 43.94));
        when(apiClient.fetchInstruments()).thenReturn(instrumentsResponse);

        // and
        List<InvestmentAccount> expected =
                buildExpectedInvestmentAccounts(43.94, -56.06, 43.94, 0.0, Collections.emptyList());

        // when
        List<InvestmentAccount> result =
                (List<InvestmentAccount>) investmentsFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
    }

    private Object[] instrumentsParams() {
        return new Object[] {
            new Object[] {new InstrumentsResponse()},
            new Object[] {
                SerializationUtils.deserializeFromString(
                        "{\"instruments\": []}", InstrumentsResponse.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(
                                        TEST_DATA_PATH,
                                        "invest_instruments_without_position_or_deleted.json")
                                .toFile(),
                        InstrumentsResponse.class)
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
            Double totalValue) {
        String investmentsResponseString =
                "{\n"
                        + "    \"portfolio\": {\n"
                        + "        \"accountId\": \"123456INET\",\n"
                        + "        \"accountNumber\": "
                        + accountNumber
                        + ",\n"
                        + "        \"cashAvailableForWithdrawal\": 25.93,\n"
                        + "        \"cashBalance\": "
                        + cashBalance
                        + ",\n"
                        + "        \"clientId\": \"12345678\",\n"
                        + "        \"created\": 1615132050000,\n"
                        + "        \"currency\": \"DKK\",\n"
                        + "        \"id\": \"06a6f622-acc2-41f8-ac89-401ec3e056df_portfolio\",\n"
                        + "        \"totalOpenPositionsValue\": "
                        + totalOpenPositionsValue
                        + ",\n"
                        + "        \"totalValue\": "
                        + totalValue
                        + ",\n"
                        + "        \"updated\": 1615132050000,\n"
                        + "        \"userStatus\": \"active\"\n"
                        + "    }\n"
                        + "}";
        return SerializationUtils.deserializeFromString(
                investmentsResponseString, InvestmentsResponse.class);
    }
}
