package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.util.concurrent.Uninterruptibles;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.Response;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final SwedbankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationController.class);

    public SwedbankTransactionFetcher(
            final SwedbankApiClient apiClient,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        try {
            return apiClient.getTransactions(
                    account.getApiIdentifier(),
                    Timestamp.valueOf(
                            LocalDateTime.now().minusMonths(TimeValues.MONTHS_TO_FETCH_MAX)),
                    toDate);

        } catch (HttpResponseException e) {
            if (checkIfScaIsRequired(e)) {
                startScaAuthorization(account.getApiIdentifier(), fromDate, toDate);
                return getTransactionsFor(account, fromDate, toDate);
            } else {
                throw e;
            }
        }
    }

    private boolean checkIfScaIsRequired(HttpResponseException e) {
        return (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                && e.getResponse()
                        .getBody(String.class)
                        .toLowerCase()
                        .contains(SwedbankConstants.ErrorMessages.SCA_REQUIRED));
    }

    private boolean checkIfScaIsAlreadyDone(HttpResponseException e) {
        return e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                && e.getResponse()
                        .getBody(String.class)
                        .toLowerCase()
                        .contains(SwedbankConstants.ErrorMessages.TRANSACTION_SCA_ALREADY_SIGNED);
    }

    private void startScaAuthorization(String account, Date fromDate, Date toDate) {
        try {
            Response response = apiClient.startScaTransactionRequest(account, fromDate, toDate);
            if (response.getStatementStatus().equalsIgnoreCase(ConsentStatus.SIGNED)) {
                return;
            }
            poll(response);
        } catch (HttpResponseException e) {
            if (checkIfScaIsAlreadyDone(e)) {
                logger.info(
                        "Request SCA, but its already been signed for the requested period, proceeding with authentication");
            }
            throw e;
        }
    }

    private void poll(Response response) {
        boolean status;

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(
                        new URL(response.getLinks().getHrefEntity().getHref())));

        for (int i = 0; i < SwedbankConstants.TimeValues.ATTEMPS_BEFORE_TIMEOUT; i++) {
            status = apiClient.getScaStatus(response.getLinks().getScaStatus().getHref());

            if (status) {
                logger.info(SwedbankConstants.LogMessages.SIGNING_COMPLETE);
                return;
            }
            logger.info(SwedbankConstants.LogMessages.WAITING_FOR_SIGNING);
            Uninterruptibles.sleepUninterruptibly(
                    SwedbankConstants.TimeValues.SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
        throw new IllegalStateException(
                SwedbankConstants.LogMessages.TRANSACTION_SIGNING_TIMED_OUT);
    }
}
