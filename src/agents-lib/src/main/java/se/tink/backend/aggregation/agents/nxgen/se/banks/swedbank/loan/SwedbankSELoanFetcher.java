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
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.CarLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.CollateralsLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate.ConspumptionLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.SwedbankDefaultLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class SwedbankSELoanFetcher extends SwedbankDefaultLoanFetcher {
    private static final AggregationLogger LOGGER = new AggregationLogger(SwedbankSELoanFetcher.class);

    private final SwedbankSEApiClient apiClient;

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

        //Collaterals
        List<CollateralsEntity> collaterals = loanOverviewResponse.getCollaterals();
        if (collaterals != null) {
            List<LoanAccount> collateralsLoans = collaterals.stream()
                    .flatMap(collateral -> collateral.getLoans().stream())
                    .map(loan -> {
                        DetailedLoanResponse loanDetails = getLoanDetails(loan);
                        return Optional.ofNullable(loanDetails).map(ld -> CollateralsLoanEntity.create(loan, ld))
                                .orElseGet(() -> CollateralsLoanEntity.create(loan));
                    })
                    .map(CollateralsLoanEntity::toTinkLoan)
                    .collect(Collectors.toList());
            loanAccounts.addAll(collateralsLoans);
        }

        //Car Loans
        List<LoanAccount> carLoans = loanOverviewResponse.getCarLoans().stream()
                .map(loan -> {
                    DetailedLoanResponse loanDetails = getLoanDetails(loan);
                    return Optional.ofNullable(loanDetails).map(ld -> CarLoanEntity.create(loan, ld))
                            .orElseGet(() -> CarLoanEntity.create(loan));
                })
                .map(CarLoanEntity::toTinkLoan)
                .collect(Collectors.toList());
        loanAccounts.addAll(carLoans);
        //Consumption Loans
        List<LoanAccount> consumptionLoans = loanOverviewResponse.getConsumptionLoans().stream()
                .map(loan -> {
                    DetailedLoanResponse loanDetails = getLoanDetails(loan);
                    return Optional.ofNullable(loanDetails).map(ld -> ConspumptionLoanEntity.create(loan, ld))
                            .orElseGet(() -> ConspumptionLoanEntity.create(loan));
                })
                .map(ConspumptionLoanEntity::toTinkLoan)
                .collect(Collectors.toList());
        loanAccounts.addAll(consumptionLoans);

        return loanAccounts;
    }

    // TODO: Follow links as long as they are there
    private DetailedLoanResponse getLoanDetails(LoanEntity loan) {
        return Optional.of(loan)
                .filter(loanEntity -> loanEntity.getLinks() != null)
                .map(LoanEntity::getLinks)
                .filter(linksEntity -> linksEntity.getNext() != null)
                .map(LinksEntity::getNext)
                .map(linkEntity -> apiClient.loadDetailsEntity(linkEntity))
                .orElseGet(() -> null);
    }
}
