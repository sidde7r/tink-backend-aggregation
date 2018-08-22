package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.AbstractInvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.DetailedPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.EndowmentInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.EquityTraderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.FundAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.InvestmentSavingsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.SavingAccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultInvestmentFetcher.class);
    private static final AggregationLogger LOGGER = new AggregationLogger(SwedbankDefaultInvestmentFetcher.class);
    private static final String LIST_INPUT_ERROR_FORMAT_MESSAGE = "{} where null, expected at least an empty list.";
    private static final String FUND_ACCOUNTS_STRING = "Fund accounts";
    private static final String EQUITY_TRADERS_STRING = "Equity traders";
    private static final String ISK_STRING = "Investment savings";
    private static final String ENDOWMENT_INSURANCE_STRING = "Endowment insurance";

    private final SwedbankDefaultApiClient apiClient;
    private final String defaultCurrency;

    public SwedbankDefaultInvestmentFetcher(SwedbankDefaultApiClient apiClient, String defaultCurrency) {
        this.apiClient = apiClient;
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        ArrayList<InvestmentAccount> investmentAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            String portfolioHoldingsString = apiClient.portfolioHoldings();

            PortfolioHoldingsResponse portfolioHoldings = SerializationUtils
                    .deserializeFromString(portfolioHoldingsString, PortfolioHoldingsResponse.class);

            if (portfolioHoldings.hasInvestments()) {
                log.info(SwedbankBaseConstants.LogTags.PORTFOLIO_HOLDINGS_RESPONSE.toString(), portfolioHoldingsString);
            }

            investmentAccounts.addAll(fundAccountsToInvestmentAccounts(
                    portfolioHoldings.getFundAccounts()));
            // temporary changed to debug
            investmentAccounts.addAll(endowmentInsurancesToTinkInvestmentAccounts(
                    portfolioHoldings.getEndowmentInsurances(), bankProfile.getEngagementOverViewResponse()));
            investmentAccounts.addAll(equityTradersToTinkInvestmentAccounts(
                    portfolioHoldings.getEquityTraders()));
            investmentAccounts.addAll(investmentSavingsToTinkInvestmentAccounts(
                    portfolioHoldings.getInvestmentSavings()));
        }

        return investmentAccounts;
    }

    private List<InvestmentAccount> equityTradersToTinkInvestmentAccounts(List<EquityTraderEntity> equityTraders) {
        if (equityTraders == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, EQUITY_TRADERS_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(getDetailedPortfolioResponseList(
                equityTraders, EQUITY_TRADERS_STRING));
    }

    private List<InvestmentAccount> investmentSavingsToTinkInvestmentAccounts(
            List<InvestmentSavingsAccountEntity> investmentSavingsAccounts) {
        if (investmentSavingsAccounts == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, ISK_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(getDetailedPortfolioResponseList(
                investmentSavingsAccounts, ISK_STRING));
    }

    private List<InvestmentAccount> endowmentInsurancesToTinkInvestmentAccounts(
            List<EndowmentInsuranceEntity> endowmentInsurances, EngagementOverviewResponse engagementOverviewResponse) {
        if (endowmentInsurances == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, ENDOWMENT_INSURANCE_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(getDetailedPortfolioResponseList(
                endowmentInsurances, ENDOWMENT_INSURANCE_STRING, engagementOverviewResponse));
    }

    private List<InvestmentAccount> defaultAccountToInvestmentAccount(
            List<DetailedPortfolioResponse> detailedPortfolioResponses) {
        return detailedPortfolioResponses.stream()
                .map(DetailedPortfolioResponse::getDetailedHolding)
                .map(detailedPortfolio -> detailedPortfolio.toTinkInvestmentAccount(apiClient, defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> fundAccountsToInvestmentAccounts(List<FundAccountEntity> fundAccounts) {
        if (fundAccounts == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, FUND_ACCOUNTS_STRING);
            return Collections.emptyList();
        }

        List<DetailedPortfolioResponse> detailedPortfolioResponses = getDetailedPortfolioResponseList(
                fundAccounts, FUND_ACCOUNTS_STRING);

        return detailedPortfolioResponses.stream()
                .map(DetailedPortfolioResponse::getDetailedHolding)
                .map(detailedPortfolio -> detailedPortfolio.toTinkFundInvestmentAccount(
                        apiClient, defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<DetailedPortfolioResponse> getDetailedPortfolioResponseList(
            List<? extends AbstractInvestmentAccountEntity> entityList, String type) {

        List<String> responseList = entityList.stream()
                .map(AbstractInvestmentAccountEntity::getLinks)
                .map(LinksEntity::getSelf)
                .map(apiClient::detailedPortfolioInfo)
                .collect(Collectors.toList());

        for (String response : responseList) {
            log.info(SwedbankBaseConstants.LogTags.DETAILED_PORTFOLIO_RESPONSE.toString(), type, response);
        }

        return responseList.stream()
                .map(responseString -> SerializationUtils.deserializeFromString(
                        responseString, DetailedPortfolioResponse.class))
                .collect(Collectors.toList());
    }

    // debug logging for Swedbank investments
    // log errors for endowment insurance details, these are too common
    // especially try to find out what type of insurance we get errors from and if possible try to figure out
    // how to retrieve that data
    private List<DetailedPortfolioResponse> getDetailedPortfolioResponseList(
            List<? extends AbstractInvestmentAccountEntity> entityList, String type,
            EngagementOverviewResponse engagementOverviewResponse) {

        List<String> responseList = new ArrayList<>();
        for (AbstractInvestmentAccountEntity accountEntity : entityList) {
            LinkEntity selfLink = accountEntity.getLinks().getSelf();
            try {
                String detailedPortfolioResponse = apiClient.detailedPortfolioInfo(selfLink);
                responseList.add(detailedPortfolioResponse);
            } catch (Exception e) {
                logFailedDetailedPortfolioFetch(accountEntity, engagementOverviewResponse);
            }
        }

        for (String response : responseList) {
            LOGGER.infoExtraLong(response, SwedbankBaseConstants.LogTags.ENDOWMENT_DETAILED_PORTFOLIO_RESPONSE);
        }

        return responseList.stream()
                .map(responseString -> SerializationUtils.deserializeFromString(
                        responseString, DetailedPortfolioResponse.class))
                .collect(Collectors.toList());
    }

    // log why some fetches for detailed endowments fail
    private void logFailedDetailedPortfolioFetch(AbstractInvestmentAccountEntity accountEntity,
            EngagementOverviewResponse engagementOverviewResponse) {

        try {
            String endowmentAccountEntity = SerializationUtils.serializeToString(accountEntity);
            String investmentInEngagement = serializeFailingAccountFromEngagements(accountEntity,
                    engagementOverviewResponse.getSavingAccounts());

            LOGGER.infoExtraLong(String.format("Failed to fetch details for engagement[%s], holdings[%s]",
                    investmentInEngagement, endowmentAccountEntity),
                    SwedbankBaseConstants.LogTags.ENDOWMENT_DETAILED_PORTFOLIO_RESPONSE);
        } catch (Exception e) {
            log.warn("Swedbank Failed to log endowment error", e);
        }
    }

    private String serializeFailingAccountFromEngagements(AbstractInvestmentAccountEntity accountEntity,
            List<SavingAccountEntity> savingAccounts) {

        Optional<SavingAccountEntity> engagementAccount = savingAccounts.stream()
                .filter(a -> a.getAccountNumber() != null &&
                        a.getAccountNumber().equalsIgnoreCase(accountEntity.getAccountNumber()) &&
                        a.getId() == null)
                .findAny();

        String investmentEngagement = "N/A";
        if (engagementAccount.isPresent()) {
            investmentEngagement = SerializationUtils.serializeToString(engagementAccount);
        }

        return investmentEngagement;
    }
}
