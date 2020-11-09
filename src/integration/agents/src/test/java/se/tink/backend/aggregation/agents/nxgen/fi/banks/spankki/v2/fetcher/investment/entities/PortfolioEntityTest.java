package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PortfolioEntityTest {

    private static final String ZERO_MARKET_VALUE =
            "{"
                    + "    \"portfolioId\": \"123456\","
                    + "    \"portfolioName\": \"S-Portfölj\","
                    + "    \"plegded\": false,"
                    + "    \"ownerName\": \"Tink Tester\","
                    + "    \"account\": null,"
                    + "    \"type\": \"S_BANK_PORTFOLIO\","
                    + "    \"positions\": [],"
                    + "    \"totalMarketValue\": 0.0,"
                    + "    \"portfolioDefaultAccount\": null,"
                    + "    \"onlyDefaultAccountForTransactions\": false,"
                    + "    \"paymentReference\": \"44332211\","
                    + "    \"portfolioAllowedUses\": {"
                    + "        \"portfolioAllowedUseType\": ["
                    + "            \"FUND_SWAP\","
                    + "            \"FUND_REDEMPTION\","
                    + "            \"FUND_SUBSCRIPTION\""
                    + "        ]"
                    + "    },"
                    + "    \"authorization\": \"PERSONAL\""
                    + "}";

    @Test
    public void shouldHandleZeroMarketValue() {
        PortfolioEntity portfolioEntity =
                SerializationUtils.deserializeFromString(ZERO_MARKET_VALUE, PortfolioEntity.class);
        PortfolioModule portfolioModule = portfolioEntity.toTinkPortfolio(Collections.emptyList());

        assertThat(portfolioModule).isNotNull();
        assertThat(portfolioModule.getUniqueIdentifier()).isEqualTo("123456");
        assertThat(portfolioModule.getTotalProfit()).isEqualTo(0.0);
    }
}
