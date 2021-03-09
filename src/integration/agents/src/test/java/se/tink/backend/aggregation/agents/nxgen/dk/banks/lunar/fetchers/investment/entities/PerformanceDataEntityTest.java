package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.PortfolioPerformanceResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class PerformanceDataEntityTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    @Test
    @Parameters(method = "totalProfitParams")
    public void shouldGetTotalProfitWithDifferentTestValues(
            PerformanceDataEntity testPerformanceDataEntity,
            BigDecimal testTotalValue,
            double expected) {

        // given & when
        double result = testPerformanceDataEntity.getTotalProfit(testTotalValue);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] totalProfitParams() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "investment_performance_data.json")
                                        .toFile(),
                                PortfolioPerformanceResponse.class)
                        .getPerformanceData(),
                BigDecimal.valueOf(43.94),
                -56.06
            },
            new Object[] {
                getTestData(BigDecimal.valueOf(-0.1234)), BigDecimal.valueOf(1082.22), -152.35
            },
            new Object[] {
                getTestData(BigDecimal.valueOf(0.1234)), BigDecimal.valueOf(1386.90), 152.34
            },
            new Object[] {getTestData(BigDecimal.valueOf(0.3333)), BigDecimal.valueOf(4), 1.00},
            new Object[] {getTestData(BigDecimal.valueOf(-0.3333)), BigDecimal.valueOf(2), -1.00},
            new Object[] {getTestData(BigDecimal.valueOf(0.6667)), BigDecimal.valueOf(5), 2.00},
            new Object[] {getTestData(BigDecimal.valueOf(-0.6667)), BigDecimal.valueOf(1), -2.00},
            new Object[] {getTestData(null), BigDecimal.valueOf(1), 0},
            new Object[] {getTestData(BigDecimal.valueOf(0.0)), BigDecimal.valueOf(0), 0},
            new Object[] {getTestData(BigDecimal.valueOf(0)), BigDecimal.valueOf(0), 0},
            new Object[] {getTestData(BigDecimal.valueOf(0.0)), BigDecimal.valueOf(2), 0},
            new Object[] {getTestData(BigDecimal.valueOf(0)), BigDecimal.valueOf(3), 0},
            new Object[] {new PerformanceDataEntity(), null, 0},
        };
    }

    private PerformanceDataEntity getTestData(BigDecimal testReturnFraction) {
        String performanceDataString =
                "{\n"
                        + "    \"performanceData\": {\n"
                        + "        \"all\": {\n"
                        + "            \"returnFraction\": "
                        + testReturnFraction
                        + "        }\n"
                        + "    }\n"
                        + "}";

        return SerializationUtils.deserializeFromString(
                        performanceDataString, PortfolioPerformanceResponse.class)
                .getPerformanceData();
    }
}
