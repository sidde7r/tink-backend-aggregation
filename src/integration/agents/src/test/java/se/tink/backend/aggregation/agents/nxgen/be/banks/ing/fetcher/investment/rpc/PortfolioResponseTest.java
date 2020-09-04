package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities.PortfoliosEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PortfolioResponseTest {

    private static final String EXAMPLE_PORTFOLIO_RESPONSE =
            "{\n"
                    + "    \"mobileResponse\": {\n"
                    + "        \"header\": {\n"
                    + "            \"version\": \"1.0\",\n"
                    + "            \"url\": \"/eb/MobileRequest\"\n"
                    + "        },\n"
                    + "        \"returnCode\": \"OK\",\n"
                    + "        \"portfolios\": {\n"
                    + "            \"portfolio\": [\n"
                    + "                {\n"
                    + "                    \"portfolioAccountNumberBBAN\": \"3800123456789\",\n"
                    + "                    \"portfolioAccountName\": \"MEJ ELS NAME\",\n"
                    + "                    \"portfolioAccountType\": \"Portfolio\",\n"
                    + "                    \"portfolioBalance\": \"+0000000000000000000000000000000098635302\"\n"
                    + "                }\n"
                    + "            ,\n"
                    + "                {\n"
                    + "                    \"portfolioAccountNumberBBAN\": \"3856423456789\",\n"
                    + "                    \"portfolioAccountName\": \"MEVR ELS NAME\",\n"
                    + "                    \"portfolioAccountType\": \"Star Fund\",\n"
                    + "                    \"portfolioBalance\": \"+0000000000000000000000000000000225043202\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        },\n"
                    + "        \"portfolioTotalBalance\": \"+323678502\"\n"
                    + "    }\n"
                    + "}";

    @Test
    public void testParsePortfolioResponse() {
        PortfolioResponse portfolioResponse =
                SerializationUtils.deserializeFromString(
                        EXAMPLE_PORTFOLIO_RESPONSE, PortfolioResponse.class);

        assertThat(portfolioResponse).isNotNull();
        assertThat(portfolioResponse.getMobileResponse()).isNotNull();
        PortfoliosEntity portfolios = portfolioResponse.getMobileResponse().getPortfolios();
        assertThat(portfolios).isNotNull();
        assertThat(portfolios.getPortfolio()).isNotNull().hasSize(2);
        assertThat(portfolios.getPortfolio().get(0))
                .hasFieldOrPropertyWithValue("portfolioAccountNumberBBAN", "3800123456789")
                .hasFieldOrPropertyWithValue("portfolioAccountName", "MEJ ELS NAME")
                .hasFieldOrPropertyWithValue("portfolioAccountType", "Portfolio")
                .hasFieldOrPropertyWithValue(
                        "portfolioBalance", "+0000000000000000000000000000000098635302");
        assertThat(portfolios.getPortfolio().get(1))
                .hasFieldOrPropertyWithValue("portfolioAccountNumberBBAN", "3856423456789")
                .hasFieldOrPropertyWithValue("portfolioAccountName", "MEVR ELS NAME")
                .hasFieldOrPropertyWithValue("portfolioAccountType", "Star Fund")
                .hasFieldOrPropertyWithValue(
                        "portfolioBalance", "+0000000000000000000000000000000225043202");
    }
}
