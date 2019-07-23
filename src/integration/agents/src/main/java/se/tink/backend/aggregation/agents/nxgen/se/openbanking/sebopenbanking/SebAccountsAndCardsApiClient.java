package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebAccountsAndCardsApiClient extends SebAbstractApiClient {

    public SebAccountsAndCardsApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequestInSession(
                        new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebAccountsAndCardsConstants.Urls.ACCOUNTS))
                .queryParam(
                        SebCommonConstants.QueryKeys.WITH_BALANCE,
                        SebCommonConstants.QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(SebCommonConstants.Urls.BASE_URL + urlAddress);

        RequestBuilder requestBuilder = createRequestInSession(url);

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    SebCommonConstants.QueryKeys.BOOKING_STATUS,
                    SebCommonConstants.QueryValues.BOOKED_TRANSACTIONS);
        }

        FetchTransactionsResponse response = requestBuilder.get(FetchTransactionsResponse.class);

        return response;
    }

    public Collection<CreditCardAccount> fetchCardAccounts() {
        return createRequestInSession(
                        new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebAccountsAndCardsConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class)
                .getTransactions();
    }

    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebAccountsAndCardsConstants.Urls
                                                .CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        return createRequestInSession(url)
                .queryParam(SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                .get(FetchCardAccountsTransactions.class);
    }
}
