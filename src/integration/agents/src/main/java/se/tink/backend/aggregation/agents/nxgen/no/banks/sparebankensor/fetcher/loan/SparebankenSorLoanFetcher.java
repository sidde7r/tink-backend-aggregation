package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import joptsimple.internal.Strings;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.rpc.SoTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SparebankenSorLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SparebankenSorLoanFetcher.class);

    private final SparebankenSorApiClient apiClient;

    public SparebankenSorLoanFetcher(SparebankenSorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        // Disclaimer! I don't know what this is, may be reasonable (?) so keeping it for logging
        try {
            // These requests seem to be chained with the loan fetching url, for now assuming that
            // they are
            // necessary for accessing the loans.
            URL fetchSoTokenUrl =
                    new URL("https://nettbank.sor.no/secesb/rest/era/ssotoken/so")
                            .queryParam("endpoint", "classic");
            SoTokenResponse soTokenResponse = apiClient.fetchSoToken(fetchSoTokenUrl);

            URL transigoLogonUrl =
                    new URL("https://nettbank.sor.no/payment/transigo/logon/done/smartbank/json")
                            .queryParam("so", soTokenResponse.getSo());
            apiClient.transigoLogon(transigoLogonUrl);

            // Not sure if this is the correct url for fetching loans, it's where the app goes if
            // you choose
            // loans although the current account is listed there as well.
            URL transigoAccountsUrl =
                    new URL("https://nettbank.sor.no/payment/transigo/json/accounts");
            String transigoAccountsResponse = apiClient.transigoAccounts(transigoAccountsUrl);

            LOGGER.infoExtraLong(
                    transigoAccountsResponse, SparebankenSorConstants.LogTags.LOAN_LOG_TAG);
        } catch (Exception e) {
            LOGGER.infoExtraLong(
                    "Failed to retrieve loans", SparebankenSorConstants.LogTags.LOAN_LOG_TAG);
        }

        List<AccountEntity> accountList = apiClient.fetchAccounts();

        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
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
            LOGGER.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " no link to loan details present.");
            return;
        }

        try {
            String loanDetailsResponse = apiClient.fetchLoanDetails(detailsLink.getHref());
            LOGGER.infoExtraLong(loanDetailsResponse, SparebankenSorConstants.LogTags.LOAN_DETAILS);

        } catch (Exception e) {
            LOGGER.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " fetching of loan details failed");
        }
    }
}
