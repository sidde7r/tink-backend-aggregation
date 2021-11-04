package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Slf4j
public class HandelsbankenLoanFetcher implements AccountFetcher<LoanAccount> {
    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final Credentials credentials;

    public HandelsbankenLoanFetcher(
            HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        try {
            return sessionStorage
                    .applicationEntryPoint()
                    .map(
                            applicationEntryPoint ->
                                    client.loans(applicationEntryPoint).toTinkLoans(credentials))
                    .orElseGet(Collections::emptyList);
        } catch (Exception e) {
            // TC-5468 All SE loan fetching attempts end with 500 response which breaks the whole
            // refresh. Temporarily return empty list when any exception is thrown while we
            // investigate the issue.
            log.warn("Fetching of loans failed, returning empty list.");
            return Collections.emptyList();
        }
    }
}
