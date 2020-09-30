package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class SparebankenSorLoanFetcher implements AccountFetcher<LoanAccount> {
    private final SparebankenSorApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<AccountEntity> accountList = apiClient.fetchAccounts();

        return Optional.ofNullable(accountList).orElseGet(Collections::emptyList).stream()
                .filter(AccountEntity::isLoanAccount)
                .map(
                        accountEntity -> {
                            logLoanDetails(accountEntity);
                            return accountEntity.toTinkLoan();
                        })
                .collect(Collectors.toList());
    }

    private void logLoanDetails(AccountEntity accountEntity) {

        LinkEntity detailsLink = accountEntity.getLinks().get(SparebankenSorConstants.Link.DETAILS);

        if (detailsLink == null || Strings.isNullOrEmpty(detailsLink.getHref())) {
            log.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " no link to loan details present.");
            return;
        }

        try {
            apiClient.fetchDetails(detailsLink.getHref());
        } catch (HttpResponseException e) {
            log.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " fetching of loan details failed",
                    e);
        }
    }
}
