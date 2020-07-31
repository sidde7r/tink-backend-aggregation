package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.rpc.SoTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparebankenSorLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            apiClient.transigoAccounts(transigoAccountsUrl);
        } catch (Exception e) {
            logger.info(
                    "tag={} Failed to retrieve loans",
                    SparebankenSorConstants.LogTags.LOAN_LOG_TAG,
                    e);
        }

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
            logger.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " no link to loan details present.");
            return;
        }

        try {
            apiClient.fetchLoanDetails(detailsLink.getHref());
        } catch (Exception e) {
            logger.warn(
                    SparebankenSorConstants.LogTags.LOAN_DETAILS.toString()
                            + " fetching of loan details failed",
                    e);
        }
    }
}
