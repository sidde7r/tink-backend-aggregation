package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate.BaseAbstractLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate.CarLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate.CollateralsLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate.ConsumptionLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate.LoanEntityFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SwedbankSELoanFetcher implements AccountFetcher<LoanAccount> {

    private final SwedbankSEApiClient apiClient;
    private final LoanEntityFactory loanEntityFactory = new LoanEntityFactory();

    public SwedbankSELoanFetcher(SwedbankSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            LoanOverviewResponse loanOverviewResponse = apiClient.loanOverview();

            if (Objects.isNull(loanOverviewResponse)) {
                continue;
            }

            fetchLoans(
                    loanAccounts,
                    loanOverviewResponse,
                    bankProfile.getEngagementOverViewResponse());
        }

        return loanAccounts;
    }

    protected void fetchLoans(
            ArrayList<LoanAccount> loanAccounts,
            LoanOverviewResponse loanOverviewResponse,
            EngagementOverviewResponse engagementOverviewResponse) {

        fetchCollateralLoans(loanAccounts, loanOverviewResponse);

        fetchCarLoans(loanAccounts, loanOverviewResponse);

        fetchConsumptionLoans(loanAccounts, loanOverviewResponse, engagementOverviewResponse);
    }

    private void fetchCollateralLoans(
            ArrayList<LoanAccount> loanAccounts, LoanOverviewResponse loanOverviewResponse) {
        List<CollateralsEntity> collaterals = loanOverviewResponse.getCollaterals();

        List<LoanAccount> collateralLoans =
                Optional.ofNullable(collaterals).orElseGet(Collections::emptyList).stream()
                        .flatMap(collateral -> collateral.getLoans().stream())
                        .map(
                                loan ->
                                        createLoanAccountFromLoanInformation(
                                                loan, CollateralsLoanEntity.class))
                        .collect(Collectors.toList());

        loanAccounts.addAll(collateralLoans);
    }

    private void fetchCarLoans(
            ArrayList<LoanAccount> loanAccounts, LoanOverviewResponse loanOverviewResponse) {
        List<LoanAccount> carLoans =
                loanOverviewResponse.getCarLoans().stream()
                        .map(
                                loan ->
                                        createLoanAccountFromLoanInformation(
                                                loan, CarLoanEntity.class))
                        .collect(Collectors.toList());

        loanAccounts.addAll(carLoans);
    }

    private void fetchConsumptionLoans(
            ArrayList<LoanAccount> loanAccounts,
            LoanOverviewResponse loanOverviewResponse,
            EngagementOverviewResponse engagementOverviewResponse) {

        loanAccounts.addAll(getFilteredOutLoans(loanOverviewResponse, engagementOverviewResponse));
    }

    private List<LoanAccount> getFilteredOutLoans(
            LoanOverviewResponse loanOverviewResponse,
            EngagementOverviewResponse engagementOverviewResponse) {
        // Filter out any account that is present in the
        // engagementOverviewResponse::transactionAccounts list.

        Stream<LoanEntity> consumptionLoans = loanOverviewResponse.getConsumptionLoans().stream();
        if (!apiClient.isRefreshOnlyLoanData()) {
            consumptionLoans =
                    consumptionLoans.filter(
                            loan ->
                                    !engagementOverviewResponse.hasTransactionAccount(
                                            loan.getAccount()));
        } else {
            consumptionLoans = consumptionLoans.filter(this::filterOutZeroDebtAccounts);
        }
        return consumptionLoans
                .map(
                        loan ->
                                createLoanAccountFromLoanInformation(
                                        loan, ConsumptionLoanEntity.class))
                .collect(Collectors.toList());
    }

    private boolean filterOutZeroDebtAccounts(LoanEntity loan) {
        return loan.getDebt() != null
                && !loan.getDebt().getAmount().isEmpty()
                && !loan.getDebt().getAmount().equals("0");
    }

    private <T extends BaseAbstractLoanEntity> LoanAccount createLoanAccountFromLoanInformation(
            LoanEntity loanEntity, Class<T> loanType) {
        return Optional.of(loanEntity)
                .map(createLoanEntityFromDetails(loanType))
                .map(BaseAbstractLoanEntity::toTinkLoan)
                .orElseThrow(IllegalStateException::new);
    }

    private <T extends BaseAbstractLoanEntity> Function<LoanEntity, T> createLoanEntityFromDetails(
            Class<T> loanType) {
        return loan -> {
            DetailedLoanResponse loanDetails = getLoanDetails(loan);
            return Optional.ofNullable(loanDetails)
                    .map(ld -> loanEntityFactory.create(loanType, loan, ld))
                    .orElseGet(() -> loanEntityFactory.create(loanType, loan));
        };
    }

    private DetailedLoanResponse getLoanDetails(LoanEntity loan) {
        return Optional.of(loan)
                .filter(loanEntity -> loanEntity.getLinks() != null)
                .map(LoanEntity::getLinks)
                .filter(linksEntity -> linksEntity.getNext() != null)
                .map(LinksEntity::getNext)
                .map(apiClient::loadDetailsEntity)
                .orElse(null);
    }
}
