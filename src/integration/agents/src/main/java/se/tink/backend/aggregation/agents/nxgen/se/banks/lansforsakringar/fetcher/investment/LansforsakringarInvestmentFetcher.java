package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.BondEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.EngagementsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.InvestmentSavingsDepotWrapperEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.InvestmentSavingsDepotWrappersEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.IskFundEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ShareEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchInstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchSecurityHoldingResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class LansforsakringarInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarInvestmentFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return Stream.of(getPensionAccounts(), getIskAccounts())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> getPensionAccounts() {
        final List<InvestmentAccount> pensionAccounts = Lists.newArrayList();
        for (EngagementsEntity engagementsEntity :
                apiClient.fetchPensionWithLifeInsurance().getResponse().getEngagements()) {
            pensionAccounts.add(
                    apiClient
                            .fetchPensionWithLifeInsuranceAgreement(engagementsEntity.getId())
                            .getResponse()
                            .getLifeInsuranceAgreement()
                            .toTinkInvestmentAccount());
        }

        // Log new entities
        FetchPensionResponse pensionResponse = apiClient.fetchPension();
        if (!pensionResponse.getIpsPensionsResponseModel().isEmpty()
                || !pensionResponse.getLivPensionsResponseModel().isPrivatPensionsEmpty()
                || !pensionResponse.getLivPensionsResponseModel().isCapitalInsurancesEmpty()) {
            logger.info("tag={} Found new unknown entity", LogTags.UNKNOWN_PENSION_TYPE);
        }
        return pensionAccounts;
    }

    private List<InvestmentAccount> getIskAccounts() {
        final InvestmentSavingsDepotWrapperEntity investmentSavingsDepotWrapper =
                apiClient.fetchISK().getInvestmentSavingsDepotWrapper();
        if (investmentSavingsDepotWrapper == null) {
            return Collections.emptyList();
        }

        final List<InvestmentAccount> investmentAccounts = Lists.newArrayList();
        for (InvestmentSavingsDepotWrappersEntity depotWrapper :
                investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers()) {
            String depotNumber = depotWrapper.getDepot().getDepotNumber();
            PortfolioModule portfolio =
                    depotWrapper
                            .getDepot()
                            .toTinkPortfolio(
                                    getPortfolioCashBalance(depotNumber),
                                    getInstruments(depotNumber));

            investmentAccounts.add(depotWrapper.getAccount().toTinkInvestmentAccount(portfolio));
        }

        return investmentAccounts;
    }

    private List<InstrumentModule> getInstruments(String depotNumber) {
        List<InstrumentModule> instruments =
                apiClient.fetchFundSecurityHoldings(depotNumber).getSecurityHoldings().getFunds()
                        .stream()
                        .map(IskFundEntity::toTinkInstrument)
                        .collect(Collectors.toList());

        FetchSecurityHoldingResponse stocksAndBonds =
                apiClient.fetchStockSecurityHoldings(depotNumber);

        for (ShareEntity shareEntity : stocksAndBonds.getSecurityHoldings().getShares()) {
            FetchInstrumentDetailsResponse instrumentDetailsResponse;
            try {
                instrumentDetailsResponse =
                        apiClient.fetchInstrumentDetails(depotNumber, shareEntity.getIsinCode());
            } catch (HttpResponseException e) {
                logger.error(
                        String.format(
                                "Failed to fetch instrument details for depo=%s with ISIN=%s",
                                depotNumber, shareEntity.getIsinCode()));
                continue;
            }
            instruments.add(
                    shareEntity.toTinkInstrument(
                            Optional.ofNullable(instrumentDetailsResponse.getInstruments())));
        }

        instruments.addAll(
                stocksAndBonds.getSecurityHoldings().getBonds().stream()
                        .map(BondEntity::toTinkInstrument)
                        .collect(Collectors.toList()));

        return instruments;
    }

    private String getPortfolioCashBalance(String depotNumber) {
        return apiClient
                .fetchPortfolioCashBalance(depotNumber)
                .getDepotCashBalance()
                .getMoneyAvailableForPurchase();
    }
}
