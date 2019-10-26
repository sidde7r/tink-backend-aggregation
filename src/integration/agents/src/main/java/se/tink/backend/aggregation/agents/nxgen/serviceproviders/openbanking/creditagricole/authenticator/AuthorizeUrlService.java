package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.BankEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class AuthorizeUrlService {

  private static AuthorizeUrlService instance;

  private AuthorizeUrlService() {}

  static AuthorizeUrlService getInstance() {
    if (instance == null) {
      instance = new AuthorizeUrlService();
    }
    return instance;
  }

  URL getUrl(final PersistentStorage persistentStorage,
                    final CreditAgricoleBaseConfiguration configuration,
                    final String state) {
    //TODO refactor exception
    final BankEnum bank = persistentStorage
        .get(CreditAgricoleBaseConstants.StorageKeys.BANK_ENUM, BankEnum.class)
        .orElseThrow(() -> new RuntimeException("Unable to load correct bank url"));

    final String clientId = configuration.getClientId();
    final String redirectUri = configuration.getRedirectUrl();

    System.out.println("https://127.0.0.1:7357/api/v1/thirdparty/callback?state="+state+"&code="+state);

    return new URL(bank.getAuthUrl())
        .queryParam(CreditAgricoleBaseConstants.QueryKeys.CLIENT_ID, clientId)
        .queryParam(
            CreditAgricoleBaseConstants.QueryKeys.RESPONSE_TYPE,
            CreditAgricoleBaseConstants.QueryValues.CODE)
        .queryParam(
            CreditAgricoleBaseConstants.QueryKeys.SCOPE,
            CreditAgricoleBaseConstants.QueryValues.SCOPE)
        .queryParam(CreditAgricoleBaseConstants.QueryKeys.REDIRECT_URI, redirectUri)
        .queryParam(CreditAgricoleBaseConstants.QueryKeys.STATE, state);
  }

}
