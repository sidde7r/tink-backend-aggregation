package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.ContextEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment.FundsPortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentAccountMapper {

    public static InvestmentAccount mapToTinkAccount(GetInvestmentsResponse investmentsResponse) {
        List<PortfolioModule> portfolios = getPortfolioModules(investmentsResponse);

        return portfolios.isEmpty()
                ? mapToTinkAccountWithoutAnyPortfolios(investmentsResponse)
                : mapToTinkAccountWihPortfolios(investmentsResponse, portfolios);
    }

    private static InvestmentAccount mapToTinkAccountWihPortfolios(
            GetInvestmentsResponse investmentsResponse, List<PortfolioModule> portfolios) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolios)
                .withCashBalance(getCashBalance(investmentsResponse))
                .withId(getInvestmentId(investmentsResponse))
                .build();
    }

    private static InvestmentAccount mapToTinkAccountWithoutAnyPortfolios(
            GetInvestmentsResponse investmentsResponse) {
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(getCashBalance(investmentsResponse))
                .withId(getInvestmentId(investmentsResponse))
                .build();
    }

    private static IdModule getInvestmentId(GetInvestmentsResponse investmentsResponse) {
        AccountDetailsEntity accountDetails = getAccountDetails(investmentsResponse);
        return IdModule.builder()
                .withUniqueIdentifier(getDossierNumber(investmentsResponse))
                .withAccountNumber(accountDetails.getId())
                .withAccountName(accountDetails.getDesc())
                .addIdentifier(
                        AccountIdentifierProvider.getAccountIdentifier(
                                accountDetails.getIban(), accountDetails.getId()))
                .build();
    }

    private static AccountDetailsEntity getAccountDetails(
            GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getHeader)
                .map(HeaderEntity::getContext)
                .map(ContextEntity::getSelectedAccountDetails)
                .get()
                .orElseThrow(
                        () -> new IllegalStateException("Selected Account information is missing"));
    }

    private static String getDossierNumber(GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getBody)
                .map(GetInvestmentsBodyEntity::getSelectedDossier)
                .orElseThrow(() -> new IllegalStateException("Selected Dossier Number is missing"));
    }

    private static List<PortfolioModule> getPortfolioModules(
            GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getBody)
                .map(GetInvestmentsBodyEntity::getFundsPortfolio)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(InvestmentAccountMapper::getPortfolio)
                .collect(Collectors.toList());
    }

    private static PortfolioModule getPortfolio(FundsPortfolioEntity fundsPortfolioEntity) {
        return PortfolioModule.builder()
                .withType(PortfolioModule.PortfolioType.OTHER) // information unavailable
                .withUniqueIdentifier(fundsPortfolioEntity.getFundCode())
                .withCashValue(getFundPrice(fundsPortfolioEntity))
                .withTotalProfit(getAppreciation(fundsPortfolioEntity))
                .withTotalValue(getTotalValue(fundsPortfolioEntity))
                .withoutInstruments()
                .build();
    }

    private static double getFundPrice(FundsPortfolioEntity fundsPortfolioEntity) {
        return Optional.of(fundsPortfolioEntity)
                .map(FundsPortfolioEntity::getFundPrice)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    private static double getAppreciation(FundsPortfolioEntity fundsPortfolioEntity) {
        return Optional.of(fundsPortfolioEntity)
                .map(FundsPortfolioEntity::getAppreciation)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    private static double getTotalValue(FundsPortfolioEntity fundsPortfolioEntity) {
        return Optional.of(fundsPortfolioEntity)
                .map(FundsPortfolioEntity::getTotalQuantity)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    private static String getCurrency(GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getBody)
                .map(GetInvestmentsBodyEntity::getCurrency)
                .orElseThrow(() -> new IllegalStateException("Currency information is missing"));
    }

    private static BigDecimal getCashBalanceValue(GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getBody)
                .map(GetInvestmentsBodyEntity::getDossierTotalValue)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Dossier Total Value information is missing"));
    }

    private static ExactCurrencyAmount getCashBalance(GetInvestmentsResponse investmentsResponse) {
        return ExactCurrencyAmount.of(
                getCashBalanceValue(investmentsResponse), getCurrency(investmentsResponse));
    }
}
