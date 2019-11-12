package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class AccountsService {

    private static AccountsService instance;

    private AccountsService() {}

    static AccountsService getInstance() {
        if (instance == null) {
            instance = new AccountsService();
        }
        return instance;
    }

    GetAccountsResponse get(
            final String baseUrl,
            final PersistentStorage persistentStorage,
            final TinkHttpClient client,
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

        final String authToken =
                "Bearer " + StorageService.getInstance().getTokenFromStorage(persistentStorage);
        return client.request(getUrl(baseUrl))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, authToken)
                .header(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress())
                .get(GetAccountsResponse.class);
    }

    private String getUrl(final String baseUrl) {
        return baseUrl + ApiServices.ACCOUNTS;
    }
}
