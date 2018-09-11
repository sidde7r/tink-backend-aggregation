package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.BaseAbstractLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.CarLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.CollateralsLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.ConsumptionLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.LoanEntityFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.SwedbankDefaultLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class SwedbankSELoanFetcher extends SwedbankDefaultLoanFetcher {

    private final SwedbankSEApiClient apiClient;
    private final LoanEntityFactory loanEntityFactory = new LoanEntityFactory();

    public SwedbankSELoanFetcher(SwedbankSEApiClient apiClient) {
        super(apiClient);
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

        LoanOverviewResponse loanOverviewResponse = apiClient.loanOverview();
        if (Objects.isNull(loanOverviewResponse)) {
            return Collections.emptyList();
        }

        fetchCollateralLoans(loanAccounts, loanOverviewResponse);

        fetchCarLoans(loanAccounts, loanOverviewResponse);

        fetchConsumptionLoans(loanAccounts, loanOverviewResponse);

        return loanAccounts;
    }

    private void fetchCollateralLoans(ArrayList<LoanAccount> loanAccounts,
            LoanOverviewResponse loanOverviewResponse) {
        List<CollateralsEntity> collaterals = loanOverviewResponse.getCollaterals();

        List<LoanAccount> collateralLoans = Optional.ofNullable(collaterals).orElseGet(Collections::emptyList)
                .stream()
                .flatMap(collateral -> collateral.getLoans().stream())
                .map(l -> createLoanAccountFromLoanInformation(l, CollateralsLoanEntity.class))
                .collect(Collectors.toList());

        loanAccounts.addAll(collateralLoans);
    }

    private void fetchCarLoans(ArrayList<LoanAccount> loanAccounts, LoanOverviewResponse loanOverviewResponse) {
        List<LoanAccount> carLoans = loanOverviewResponse.getCarLoans().stream()
                .map(loan -> createLoanAccountFromLoanInformation(loan, CarLoanEntity.class))
                .collect(Collectors.toList());

        loanAccounts.addAll(carLoans);
    }

    private void fetchConsumptionLoans(ArrayList<LoanAccount> loanAccounts,
            LoanOverviewResponse loanOverviewResponse) {

        EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();

        // Filter out any account that is present in the engagementOverviewResponse::transactionAccounts list.
        List<LoanAccount> consumptionLoans = loanOverviewResponse.getConsumptionLoans().stream()
                .filter(loan -> !engagementOverviewResponse.hasTransactionAccount(loan.getAccount()))
                .map(loan -> createLoanAccountFromLoanInformation(loan, ConsumptionLoanEntity.class))
                .collect(Collectors.toList());

        loanAccounts.addAll(consumptionLoans);
    }

    private <T extends BaseAbstractLoanEntity> LoanAccount createLoanAccountFromLoanInformation(
            LoanEntity loanEntity, Class<T> loanType) {
        return Optional.of(loanEntity)
                .map(loan -> {
                    DetailedLoanResponse loanDetails = getLoanDetails(loan);
                    return Optional.ofNullable(loanDetails)
                            .map(ld -> loanEntityFactory.create(loanType, loan, ld))
                            .orElseGet(() -> loanEntityFactory.create(loanType, loan));
                })
                .map(BaseAbstractLoanEntity::toTinkLoan)
                .orElseThrow(IllegalStateException::new);
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
