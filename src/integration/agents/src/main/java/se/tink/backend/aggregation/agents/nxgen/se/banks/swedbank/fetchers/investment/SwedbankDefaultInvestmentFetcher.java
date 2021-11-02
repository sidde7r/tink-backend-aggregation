package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.ENDOWMENT_INSURANCE_STRING;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.EQUITY_TRADERS_STRING;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.FUND_ACCOUNTS_STRING;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.ISK_STRING;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.LIST_INPUT_ERROR_FORMAT_MESSAGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SwedbankDefaultInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final SwedbankSEApiClient apiClient;
    private final String defaultCurrency;
    private final CredentialsRequest credentialsRequest;
    private final SystemUpdater systemUpdater;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        ArrayList<InvestmentAccount> investmentAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            if (isAuthorized(MenuItemKey.PORTFOLIOS)) {

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
            if (isAuthorized(MenuItemKey.PENSION_PORTFOLIOS)) {
                PensionPortfoliosResponse pensionPortfolios = apiClient.getPensionPortfolios();
                investmentAccounts.addAll(pensionAccountsToInvestmentAccounts(pensionPortfolios));
            }
        }

        return investmentAccounts;
    }

    private boolean isAuthorized(MenuItemKey itemKey) {
        return apiClient.getBankProfileHandler().isAuthorizedForAction(itemKey);
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
                                getPrivatePensionInsurances(pensionPortfoliosResponse).stream(),
                                getOccupationalPensionInsurances(pensionPortfoliosResponse)
                                        .stream())
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

    private List<PensionInsuranceEntity> getOccupationalPensionInsurances(
            PensionPortfoliosResponse pensionPortfoliosResponse) {
        return pensionPortfoliosResponse.getOccupationalPensionInsurances().getPensionInsurances();
    }

    private List<PensionInsuranceEntity> getPrivatePensionInsurances(
            PensionPortfoliosResponse pensionPortfoliosResponse) {
        return pensionPortfoliosResponse.getPrivatePensionInsurances().getPensionInsurances();
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
