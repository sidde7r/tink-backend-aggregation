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
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CrossKeyLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(CrossKeyLoanFetcher.class);
    private static final AggregationLogger MESSAGE_LOGGER =
            new AggregationLogger(CrossKeyLoanFetcher.class);

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
                                if (!account.isKnownLoanType()) {
                                    logLoanDetails(account, loanDetails);
                                }
                                return account.toLoanAccount(agentConfiguration, loanDetails);
                            })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.warn("Unable to fetch loan data", e);
        }
        return Collections.emptyList();
    }

    private void logLoanDetails(CrossKeyAccount account, LoanDetailsEntity loanDetails) {
        String logLine =
                String.format(
                        "Account: %s\nLoanDetails: %s",
                        SerializationUtils.serializeToString(account),
                        SerializationUtils.serializeToString(loanDetails));
        MESSAGE_LOGGER.infoExtraLong(logLine, agentConfiguration.getLoanLogTag());
    }
}
