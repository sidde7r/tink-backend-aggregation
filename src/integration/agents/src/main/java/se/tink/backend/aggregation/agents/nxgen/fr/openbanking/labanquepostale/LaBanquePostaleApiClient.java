package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class LaBanquePostaleApiClient extends BerlinGroupApiClient<LaBanquePostaleConfiguration> {

  public LaBanquePostaleApiClient(TinkHttpClient client, SessionStorage sessionStorage){
    this.client = client;
    this.sessionStorage = sessionStorage;
  }



  @Override
  public AccountsBaseResponse fetchAccounts(){
    final String clientId = getConfiguration().getClientId();
    final String clientSecret = getConfiguration().getClientSecret();
    AccountResponse accountResponse =  buildRequestWithSignature(String.format(Urls.FETCH_ACCOUNTS, clientId, clientSecret), "")
        .get(AccountResponse.class);

    for(AccountEntity accountEntity : accountResponse.getAccounts()){
      BalanceResponse balanceResponse = buildRequestWithSignature(String.format(Urls.FETCH_BALANCES, accountEntity.getResourceId(), clientId, clientSecret), "")
          .get(BalanceResponse.class);
      accountEntity.setBalances(balanceResponse.getBalances());
    }
    return accountResponse.toBerlingGroupAccountBaseResponse();
  }

  @Override
  public RequestBuilder getAccountsRequestBuilder(String url){
    return client.request(url);

  }


  public TransactionResponse fetchTransactionsLaBanquePortal(String url) {
    if(url.startsWith("v1/")){
      url = url.substring(3);  //they return the url with prefix v1/ but api is without that url, so if the prefix exists it is removed in this line
    }
    final String clientId = getConfiguration().getClientId();
    final String clientSecret = getConfiguration().getClientSecret();

    return buildRequestWithSignature(String.format(Urls.BASE_URL_WITH_SLASH + url + QueryValues.QUERY, clientId, clientSecret), "")
        .get(TransactionResponse.class);
  }

  @Override
  public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
   return null;
  }

  public String getXRequestID(){
    return UUID.randomUUID().toString();
  }

  private RequestBuilder buildRequestWithSignature(final String url, final String payload){
    final String reqId = BerlinGroupUtils.getRequestId();
    final String date = getFormattedDate();
    final String digest = generateDigest(payload);

    return client.request(url)
        .header(HeaderKeys.SIGNATURE, getAuthorization(digest, reqId))
        .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, getX_Request_Id());
  }

  private String getFormattedDate() {
    return BerlinGroupUtils.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
  }

  private String generateDigest(final String data) {
    return Signature.DIGEST_PREFIX + BerlinGroupUtils.calculateDigest(data);
  }

  private String getSignature(final String digest, String requestId){
    final String clientSigningKeyPath = getConfiguration().getClientSigningKeyPath();

    final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

    return BerlinGroupUtils.generateSignature(
        signatureEntity.toString(), clientSigningKeyPath, Signature.SIGNING_ALGORITHM);
  }

  private String getAuthorization(final String digest, String requestId){
    final String clientId = getConfiguration().getClientId();

    return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
  }

  private String getX_Request_Id(){
    return UUID.randomUUID().toString();
  }

  @Override
  public OAuth2Token getToken(String code) {
    return null;
  }

  @Override
  public String getConsentId() {
    return null;
  }

  @Override
  public OAuth2Token refreshToken(String token) {
    return null;
  }

  @Override
  public URL getAuthorizeUrl(String state) {
    return null;
  }
}
