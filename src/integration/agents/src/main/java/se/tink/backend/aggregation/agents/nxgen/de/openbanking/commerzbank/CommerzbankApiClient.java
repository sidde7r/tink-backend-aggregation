package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CommerzbankApiClient extends Xs2aDevelopersApiClient {

    public CommerzbankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }

    public ConsentResponse getConsent(PostConsentBody postConsentBody) {
        return createRequest(new URL(getConfiguration().getBaseUrl() + ApiServices.POST_CONSENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getRedirectUrl())
                .header(HeaderKeys.PSU_IP_ADDRESS, QueryValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .body(postConsentBody)
                .post(ConsentResponse.class);
    }

    public AccountsResponse createAccount() {
        return createFetchingRequest(
                        new URL(getConfiguration().getBaseUrl() + ApiServices.GET_ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public GetBalanceResponse createBalance(String href) {
        return createFetchingRequest(new URL(href)).get(GetBalanceResponse.class);
    }

    public TransactionsResponse createTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createFetchingRequest(
                        new URL(getConfiguration().getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(TransactionsResponse.class);
    }
}
