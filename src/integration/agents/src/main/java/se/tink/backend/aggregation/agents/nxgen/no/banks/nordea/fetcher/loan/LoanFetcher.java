package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity.OwnersEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.InterestRateConverter;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class LoanFetcher implements AccountFetcher<LoanAccount> {
    private FetcherClient fetcherClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return fetcherClient.fetchLoans().getLoans().stream()
                .map(x -> fetcherClient.fetchLoanDetails(x.getLoanId()))
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
                                .addIdentifier(new NorwegianIdentifier(loanDetails.getLoanId()))
                                .setProductName(loanDetails.getProductCode())
                                .build())
                .build();
    }

    private LoanModule getLoanModule(LoanDetailsResponse loanDetails) {
        List<String> applicants = getApplicants(loanDetails);
        return LoanModule.builder()
                .withType(loanDetails.getTinkLoanType())
                .withBalance(getBalance(loanDetails))
                .withInterestRate(
                        InterestRateConverter.toDecimalValue(
                                loanDetails.getInterest().getRate(), 6))
                .setAmortized(getPaid(loanDetails))
                .setInitialBalance(getInitialBalance(loanDetails))
                .setApplicants(applicants)
                .setCoApplicant(applicants.size() > 1)
                .setLoanNumber(loanDetails.getLoanId())
                .build();
    }

    private ExactCurrencyAmount getBalance(LoanDetailsResponse loanDetails) {
        final BigDecimal balance =
                Optional.ofNullable(loanDetails.getCredit())
                        .map(CreditEntity::getAvailable)
                        .orElse(loanDetails.getAmount().getBalance());
        return new ExactCurrencyAmount(balance, loanDetails.getCurrency());
    }

    private ExactCurrencyAmount getInitialBalance(LoanDetailsResponse loanDetails) {
        final BigDecimal initialBalance =
                Optional.ofNullable(loanDetails.getCredit())
                        .map(CreditEntity::getLimit)
                        .orElse(loanDetails.getAmount().getGranted());
        return new ExactCurrencyAmount(initialBalance, loanDetails.getCurrency());
    }

    private ExactCurrencyAmount getPaid(LoanDetailsResponse loanDetails) {
        final BigDecimal paid =
                Optional.ofNullable(loanDetails.getCredit())
                        .map(CreditEntity::getSpent)
                        .orElse(loanDetails.getAmount().getPaid());
        return new ExactCurrencyAmount(paid, loanDetails.getCurrency());
    }

    private List<String> getApplicants(LoanDetailsResponse loanDetails) {
        return loanDetails.getOwners().stream()
                .map(OwnersEntity::getName)
                .collect(Collectors.toList());
    }
}
