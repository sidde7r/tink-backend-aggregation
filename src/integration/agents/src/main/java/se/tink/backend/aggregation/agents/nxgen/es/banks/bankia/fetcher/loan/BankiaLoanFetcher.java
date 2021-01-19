package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan;

import io.vavr.control.Either;
import io.vavr.control.Try;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Slf4j
public class BankiaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final BankiaApiClient apiClient;

    public BankiaLoanFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return Try.of(apiClient::getLoans)
                .map(
                        loanAccountEntities ->
                                loanAccountEntities.stream()
                                        .map(
                                                loanAccountEntity ->
                                                        toLoanAccount().apply(loanAccountEntity))
                                        .filter(
                                                either -> {
                                                    if (either.isLeft()) {
                                                        log.info(either.getLeft().getMessage());
                                                        return false;
                                                    } else {
                                                        return true;
                                                    }
                                                })
                                        .map(Either::get)
                                        .collect(Collectors.toList()))
                .onFailure(e -> log.warn("Failed to fetch loan data", e))
                .getOrElse(Collections::emptyList);
    }

    private Function<LoanAccountEntity, Either<LoanDetailsErrorCode, LoanAccount>> toLoanAccount() {
        return loanAccountEntity ->
                apiClient
                        .getLoanDetails(
                                new LoanDetailsRequest(loanAccountEntity.getProductCode())
                                        .setLoanIdentifier(loanAccountEntity.getLoanIdentifier()))
                        .map(loanAccountEntity::toTinkLoanAccount);
    }
}
