package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class LaCaixaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LaCaixaApiClient apiClient;

    public LaCaixaAccountFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        UserDataResponse userDataResponse = apiClient.fetchIdentityData();

        try {
            ListAccountsResponse accountResponse = apiClient.fetchAccountList();

            if (accountResponse == null || !accountResponse.hasAccounts()) {
                return Collections.emptyList();
            }

            return accountResponse.getTransactionalAccounts(userDataResponse.getHolderName());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);

                if (errorResponse.isNoAccounts()) {
                    return Collections.emptyList();
                }
            }

            throw e;
        }
    }
}
