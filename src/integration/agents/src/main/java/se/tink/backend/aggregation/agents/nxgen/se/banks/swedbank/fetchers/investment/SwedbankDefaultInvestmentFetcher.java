package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.PensionInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.AbstractInvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.DetailedPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.DetailedPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.EndowmentInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.EquityTraderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.FundAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.InvestmentSavingsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PensionPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.MenuItemKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public class SwedbankDefaultInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger log =
            LoggerFactory.getLogger(SwedbankDefaultInvestmentFetcher.class);
    private static final String LIST_INPUT_ERROR_FORMAT_MESSAGE =
            "{} where null, expected at least an empty list.";
    private static final String FUND_ACCOUNTS_STRING = "Fund accounts";
    private static final String EQUITY_TRADERS_STRING = "Equity traders";
    private static final String ISK_STRING = "Investment savings";
    private static final String ENDOWMENT_INSURANCE_STRING = "Endowment insurance";

    private final SwedbankSEApiClient apiClient;
    private final String defaultCurrency;
    private final CredentialsRequest credentialsRequest;
    private final SystemUpdater systemUpdater;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        ArrayList<InvestmentAccount> investmentAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            if (apiClient.getBankProfileHandler().isAuthorizedForAction(MenuItemKey.PORTFOLIOS)) {

                PortfolioHoldingsResponse portfolioHoldings = apiClient.portfolioHoldings();

                investmentAccounts.addAll(
                        fundAccountsToInvestmentAccounts(portfolioHoldings.getFundAccounts()));

                investmentAccounts.addAll(
                        endowmentInsurancesToTinkInvestmentAccounts(
                                portfolioHoldings.getEndowmentInsurances()));

                investmentAccounts.addAll(
                        equityTradersToTinkInvestmentAccounts(
                                portfolioHoldings.getEquityTraders()));

                investmentAccounts.addAll(
                        investmentSavingsToTinkInvestmentAccounts(
                                portfolioHoldings.getInvestmentSavings()));
            }
            if (apiClient
                    .getBankProfileHandler()
                    .isAuthorizedForAction(MenuItemKey.PENSION_PORTFOLIOS)) {
                PensionPortfoliosResponse pensionPortfolios = apiClient.getPensionPortfolios();
                investmentAccounts.addAll(pensionAccountsToInvestmentAccounts(pensionPortfolios));
            }
        }

        return investmentAccounts;
    }

    private List<InvestmentAccount> equityTradersToTinkInvestmentAccounts(
            List<EquityTraderEntity> equityTraders) {
        if (equityTraders == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, EQUITY_TRADERS_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(getDetailedPortfolioResponseList(equityTraders));
    }

    private List<InvestmentAccount> investmentSavingsToTinkInvestmentAccounts(
            List<InvestmentSavingsAccountEntity> investmentSavingsAccounts) {
        if (investmentSavingsAccounts == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, ISK_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(
                getDetailedPortfolioResponseList(investmentSavingsAccounts));
    }

    private List<InvestmentAccount> endowmentInsurancesToTinkInvestmentAccounts(
            List<EndowmentInsuranceEntity> endowmentInsurances) {
        if (endowmentInsurances == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, ENDOWMENT_INSURANCE_STRING);
            return Collections.emptyList();
        }

        return defaultAccountToInvestmentAccount(
                getDetailedPortfolioResponseList(endowmentInsurances));
    }

    private List<InvestmentAccount> defaultAccountToInvestmentAccount(
            List<DetailedPortfolioResponse> detailedPortfolioResponses) {
        return detailedPortfolioResponses.stream()
                .map(DetailedPortfolioResponse::getDetailedHolding)
                .map(
                        detailedPortfolio ->
                                detailedPortfolio.toTinkInvestmentAccount(
                                        apiClient, defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> fundAccountsToInvestmentAccounts(
            List<FundAccountEntity> fundAccounts) {
        if (fundAccounts == null) {
            log.warn(LIST_INPUT_ERROR_FORMAT_MESSAGE, FUND_ACCOUNTS_STRING);
            return Collections.emptyList();
        }

        List<DetailedPortfolioResponse> detailedPortfolioResponses =
                getDetailedPortfolioResponseList(fundAccounts);

        return detailedPortfolioResponses.stream()
                .map(DetailedPortfolioResponse::getDetailedHolding)
                .map(
                        detailedPortfolio ->
                                detailedPortfolio.toTinkFundInvestmentAccount(
                                        apiClient, defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> pensionAccountsToInvestmentAccounts(
            PensionPortfoliosResponse pensionPortfoliosResponse) {

        List<PensionInsuranceEntity> pensionAccounts =
                Stream.concat(
                                pensionPortfoliosResponse.getPrivatePensionInsurances()
                                        .getPensionInsurances().stream(),
                                pensionPortfoliosResponse.getOccupationalPensionInsurances()
                                        .getPensionInsurances().stream())
                        .collect(Collectors.toList());

        List<DetailedPensionResponse> detailedPensionResponses =
                getDetailedPensionReponseList(pensionAccounts);

        return detailedPensionResponses.stream()
                .map(DetailedPensionResponse::getDetailedPension)
                .map(
                        detailedPension ->
                                detailedPension.toTinkInvestmentAccount(
                                        apiClient, credentialsRequest.getAccounts(), systemUpdater))
                .collect(Collectors.toList());
    }

    private List<DetailedPensionResponse> getDetailedPensionReponseList(
            List<? extends AbstractInvestmentAccountEntity> entityList) {
        return entityList.stream()
                .map(AbstractInvestmentAccountEntity::getLinks)
                .map(LinksEntity::getSelf)
                .map(apiClient::detailedPensionInfo)
                .collect(Collectors.toList());
    }

    private List<DetailedPortfolioResponse> getDetailedPortfolioResponseList(
            List<? extends AbstractInvestmentAccountEntity> entityList) {

        return entityList.stream()
                .map(AbstractInvestmentAccountEntity::getLinks)
                .map(LinksEntity::getSelf)
                .map(apiClient::detailedPortfolioInfo)
                .collect(Collectors.toList());
    }
}
