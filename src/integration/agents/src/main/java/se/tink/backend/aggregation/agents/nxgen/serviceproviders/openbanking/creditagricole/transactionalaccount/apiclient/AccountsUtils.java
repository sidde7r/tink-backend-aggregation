package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class AccountsUtils {

    static GetAccountsResponse get(
            final PersistentStorage persistentStorage,
            final TinkHttpClient client,
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

        final String authToken = "Bearer " + StorageUtils.getTokenFromStorage(persistentStorage);
        return client.request(getUrl(creditAgricoleConfiguration.getBaseUrl()))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, authToken)
                .header(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress())
                .get(GetAccountsResponse.class);
    }

    private static String getUrl(final String baseUrl) {
        return baseUrl + ApiServices.ACCOUNTS;
    }
}
