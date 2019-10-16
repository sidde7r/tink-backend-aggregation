package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class CrossKeyLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(CrossKeyLoanFetcher.class);

    private final CrossKeyApiClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyLoanFetcher(CrossKeyApiClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            return client.fetchAccounts().getAccounts().stream()
                    .filter(CrossKeyAccount::isLoan)
                    .map(
                            account -> {
                                LoanDetailsEntity loanDetails =
                                        client.fetchLoanDetails(account).getLoanDetails();
                                return account.toLoanAccount(agentConfiguration, loanDetails);
                            })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.warn("Unable to fetch loan data", e);
        }
        return Collections.emptyList();
    }
}
