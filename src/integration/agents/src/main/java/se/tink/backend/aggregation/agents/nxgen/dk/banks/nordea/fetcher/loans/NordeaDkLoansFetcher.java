package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.FinancedObjectEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.OwnersEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class NordeaDkLoansFetcher implements AccountFetcher<LoanAccount> {

    private final NordeaDkApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.getLoans().getLoans().stream()
                .map(LoanEntity::getLoanId)
                .map(apiClient::getLoanDetails)
                .map(this::toTinkLoanAccount)
                .collect(Collectors.toList());
    }

    private LoanAccount toTinkLoanAccount(LoanDetailsResponse loanDetails) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule(loanDetails))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanDetails.getLoanId())
                                .withAccountNumber(loanDetails.getLoanFormattedId())
                                .withAccountName(loanDetails.getNickname())
                                .addIdentifier(new DanishIdentifier(loanDetails.getLoanId()))
                                .setProductName(loanDetails.getProductCode())
                                .build())
                .addHolders()
                .build();
    }

    private LoanModule getLoanModule(LoanDetailsResponse loanDetails) {
        List<String> applicants = getApplicants(loanDetails);
        return LoanModule.builder()
                .withType(loanDetails.getTinkLoanType())
                .withBalance(getLoanBalance(loanDetails))
                .withInterestRate(loanDetails.getInterest().getRate())
                .setAmortized(getAmountPaid(loanDetails))
                .setInitialBalance(getInitialBalance(loanDetails))
                .setApplicants(applicants)
                .setCoApplicant(applicants.size() > 1)
                .setLoanNumber(loanDetails.getLoanId())
                .setNextDayOfTermsChange(
                        loanDetails.getInterest().getInterestChangeDateAsLocalDate())
                .setSecurity(getLoanSecurity(loanDetails))
                .build();
    }

    private ExactCurrencyAmount getLoanBalance(LoanDetailsResponse loanDetails) {
        return new ExactCurrencyAmount(
                loanDetails.getAmount().getBalance(), loanDetails.getCurrency());
    }

    private ExactCurrencyAmount getAmountPaid(LoanDetailsResponse loanDetails) {
        return Optional.ofNullable(loanDetails.getAmount().getPaid())
                .map(paid -> new ExactCurrencyAmount(paid, loanDetails.getCurrency()))
                .orElse(null);
    }

    private ExactCurrencyAmount getInitialBalance(LoanDetailsResponse loanDetails) {
        return Optional.ofNullable(loanDetails.getAmount().getGranted())
                .map(granted -> new ExactCurrencyAmount(granted, loanDetails.getCurrency()))
                .orElse(null);
    }

    private List<String> getApplicants(LoanDetailsResponse loanDetails) {
        return loanDetails.getOwners().stream()
                .map(OwnersEntity::getName)
                .collect(Collectors.toList());
    }

    private String getLoanSecurity(LoanDetailsResponse loanDetails) {
        return Optional.ofNullable(loanDetails.getFinancedObjectEntity())
                .map(FinancedObjectEntity::getName)
                .orElse(null);
    }
}
