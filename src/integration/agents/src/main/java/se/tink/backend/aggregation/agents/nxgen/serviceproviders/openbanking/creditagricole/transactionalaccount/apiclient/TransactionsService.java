package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import javax.ws.rs.core.MediaType;

class TransactionsService {

  private static TransactionsService instance;

  private TransactionsService() {}

  static TransactionsService getInstance() {
    if (instance == null) {
      instance = new TransactionsService();
    }
    return instance;
  }

  GetTransactionsResponse get(final String id,
                                     final String baseUrl,
                                     final PersistentStorage persistentStorage,
                                     final TinkHttpClient client,
                                     final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

    final String authToken = "Bearer " + StorageService.getInstance()
        .getTokenFromStorage(persistentStorage);
    return client.request(getUrl(baseUrl, id))
        .accept(MediaType.APPLICATION_JSON)
        .type(MediaType.APPLICATION_JSON)
        .header(HeaderKeys.AUTHORIZATION, authToken)
        .header(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress())
        .get(GetTransactionsResponse.class);
  }

  String getUrl(final String baseUrl, final String id) {
    return (new URL(baseUrl + ApiServices.TRANSACTIONS))
        .parameter(IdTags.ACCOUNT_ID, id)
        .get();
  }

}
