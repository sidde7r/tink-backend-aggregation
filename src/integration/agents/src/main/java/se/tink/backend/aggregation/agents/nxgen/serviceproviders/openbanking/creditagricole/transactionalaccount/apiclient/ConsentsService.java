package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.PutConsentsRequest;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class ConsentsService {

    private static ConsentsService instance;

    private ConsentsService() {}

    static ConsentsService getInstance() {
        if (instance == null) {
            instance = new ConsentsService();
        }
        return instance;
    }

    void put(
            final String baseUrl,
            final PersistentStorage persistentStorage,
            final TinkHttpClient client,
            final List<AccountIdEntity> listOfNecessaryConstents,
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

        final String authToken =
                "Bearer " + StorageService.getInstance().getTokenFromStorage(persistentStorage);

        client.request(getUrl(baseUrl))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, authToken)
                .header(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress())
                .put(buildBody(listOfNecessaryConstents));
    }

    private String getUrl(final String baseUrl) {
        return baseUrl + ApiServices.CONSENTS;
    }

    private PutConsentsRequest buildBody(final List<AccountIdEntity> listOfNecessaryConstents) {
        return new PutConsentsRequest(
                listOfNecessaryConstents, listOfNecessaryConstents, false, false);
    }
}
