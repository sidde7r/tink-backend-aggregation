package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenAccount;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AlandsBankenLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(AlandsBankenLoanFetcher.class);

    private final AlandsBankenApiClient client;

    public AlandsBankenLoanFetcher(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            return client.fetchAccounts().getAccounts().stream()
                    .filter(AlandsBankenAccount::isLoan)
                    .map(account -> {
                        LoanDetailsEntity loanDetails = client.fetchLoanDetails(account).getLoanDetails();
                        if (!account.isKnownLoanType()) {
                            logLoanDetails(account, loanDetails);
                        }
                        return account.toLoanAccount(loanDetails);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Unable to fetch loan data", e);
        }
        return Collections.emptyList();
    }

    private void logLoanDetails(AlandsBankenAccount account, LoanDetailsEntity loanDetails) {
        String logLine = String.format("Account: %s\nLoanDetails: %s",
                SerializationUtils.serializeToString(account),
                SerializationUtils.serializeToString(loanDetails));
        log.infoExtraLong(logLine, AlandsBankenConstants.Fetcher.LOAN_LOGGING);

    }
}
