package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class TransactionsUtils {

    static GetTransactionsResponse get(
            final String id,
            final URL next,
            final PersistentStorage persistentStorage,
            final TinkHttpClient client,
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

        final String authToken = "Bearer " + StorageUtils.getTokenFromStorage(persistentStorage);
        return client.request(getUrl(creditAgricoleConfiguration.getBaseUrl(), id))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, authToken)
                .header(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress())
                .get(GetTransactionsResponse.class);
    }

    static String getUrl(final String baseUrl, final String id) {
        return (new URL(baseUrl + ApiServices.TRANSACTIONS)).parameter(IdTags.ACCOUNT_ID, id).get();
    }
}
