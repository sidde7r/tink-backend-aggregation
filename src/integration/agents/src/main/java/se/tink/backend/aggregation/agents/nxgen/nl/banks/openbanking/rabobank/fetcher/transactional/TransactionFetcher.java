package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public final class TransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionFetcher.class);
    private final RabobankApiClient apiClient;

    public TransactionFetcher(final RabobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, final Date fromDate, final Date toDate) {

        final String consentStatus = apiClient.getConsentStatus();

        if (StringUtils.containsIgnoreCase(consentStatus, RabobankConstants.Consents.EXPIRE)) {
            throw BankServiceError.CONSENT_EXPIRED.exception();
        } else if (StringUtils.containsIgnoreCase(
                consentStatus, RabobankConstants.Consents.INVALID)) {
            throw BankServiceError.CONSENT_INVALID.exception();
        } else if (Objects.equals(consentStatus, RabobankConstants.Consents.REVOKED_BY_USER)) {
            throw BankServiceError.CONSENT_REVOKED_BY_USER.exception();
        } else {
            logger.warn("Consent status is " + consentStatus);
        }

        return apiClient.getTransactions(account, fromDate, toDate, false);
    }
}
