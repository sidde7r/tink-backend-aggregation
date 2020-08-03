package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankPortfolioRootEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OpBankApiClient client;
    private final Credentials credentials;

    public OpBankInvestmentFetcher(OpBankApiClient client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        logger.info(
                "tag={} Summary: {}",
                OpBankConstants.Fetcher.INVESTMENT_LOGGING,
                client.fetchTradingAssetsSummary());

        OpBankPortfolioRootEntity tradingAssetsPortfolios = client.fetchTradingAssetsPortfolios();

        return tradingAssetsPortfolios.getPortfolios().stream()
                .map(
                        tradingAssetPortfolio ->
                                client.fetchTradingAssetsPortfolioDetails(
                                        tradingAssetPortfolio.getPortfolioId()))
                .map(responseString -> logUnknownInstrumentGroupsAndParse(responseString))
                .filter(PortfolioDetailsResponse::hasInvestmentGroups)
                .map(PortfolioDetailsResponse::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    // this logging is until we have found all instrument group types
    private PortfolioDetailsResponse logUnknownInstrumentGroupsAndParse(String portfolioDetails) {
        Matcher instrumentGroupMatcher =
                OpBankConstants.Fetcher.EXTRACT_INSTRUMENT_GROUP_PATTERN.matcher(portfolioDetails);
        while (instrumentGroupMatcher.find()) {
            if (instrumentGroupMatcher.groupCount() > 0) {
                if (!OpBankConstants.Fetcher.KNOWN_PORTFOLIO_TYPES.contains(
                        instrumentGroupMatcher.group(1).toLowerCase())) {
                    logger.info(
                            "tag={} Property name: {}, type: {}",
                            OpBankConstants.Fetcher.INVESTMENT_PORTFOLIO_TYPE_LOGGING,
                            instrumentGroupMatcher.group(0),
                            instrumentGroupMatcher.group(1));
                }
            }
        }

        return SerializationUtils.deserializeFromString(
                portfolioDetails, PortfolioDetailsResponse.class);
    }
}
