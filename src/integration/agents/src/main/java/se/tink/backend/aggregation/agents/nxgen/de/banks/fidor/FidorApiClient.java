package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor;

import java.util.Date;
import java.util.NoSuchElementException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.OpenApiRateLimitEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.OpenTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FidorApiClient{

    private final TinkHttpClient client;
    private final PersistentStorage storage;
    Logger logger = LoggerFactory.getLogger(FidorApiClient.class);

    public FidorApiClient(TinkHttpClient client, PersistentStorage storage){
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String authorization){
        return client.request(new URL(baseUrl + resource))
                .header(HttpHeaders.AUTHORIZATION, authorization);
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String authorization, String contentType){
        return getRequest(baseUrl, resource, authorization)
                .header(HttpHeaders.CONTENT_TYPE, contentType);
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String authorization, String contentType, String accept){
        return getRequest(baseUrl, resource, authorization, contentType)
                .header(HttpHeaders.ACCEPT, accept);
    }


    public boolean isSessionAlive(){
        return storage.containsKey(FidorConstants.STORAGE.OAUTH_TOKEN);
    }

    public boolean validateToken(OpenTokenEntity tokenEntity){

        Date endDate = new Date(new Date().getTime() + tokenEntity.getExpiresIn() * 1000);

        if(new Date().after(endDate)){
            OpenTokenEntity token = refreshOpenApiToken(tokenEntity);
            this.storage.put(FidorConstants.STORAGE.OAUTH_TOKEN, token);
        }

        return true;
    }

    public void clearPersistentStorage(){
        this.storage.clear();
    }

    public OpenTokenEntity getTokenFromStorage(){

        OpenTokenEntity token = storage.get(FidorConstants.STORAGE.OAUTH_TOKEN, OpenTokenEntity.class)
                .orElseThrow(() -> new NoSuchElementException("Token missing"));

        validateToken(token);

        return token;
    }

    public OpenTokenEntity getOpenApiToken(String base64Basic, String code, String redirectUri, String clientId){

        String basic = FidorConstants.BASIC + base64Basic;

        OpenTokenEntity token = getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE,
                FidorConstants.URL.OPENAPI.OAUTH_TOKEN,
                basic,
                MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(FidorConstants.BODY.OPENAPI.GRANT_TYPE, FidorConstants.BODY.OPENAPI.GRANT_TYPE_AUTHORIZATION_CODE)
                .queryParam(FidorConstants.BODY.OPENAPI.CODE, code)
                .queryParam(FidorConstants.BODY.OPENAPI.REDIRECT_URI, FidorConstants.SANDBOX_REDIRECT_URL)
                .queryParam(FidorConstants.BODY.OPENAPI.CLIENT_ID, clientId)
                .post(OpenTokenEntity.class);

        storage.put(FidorConstants.STORAGE.OAUTH_TOKEN, token);
        return token;
    }

    public OpenTokenEntity refreshOpenApiToken(OpenTokenEntity tokenEntity){

        String basic = FidorConstants.BASIC + FidorConstants.SANDBOX_BASE64_BASIC_AUTH;

        return getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE, FidorConstants.URL.OPENAPI.OAUTH_TOKEN,
                FidorConstants.SANDBOX_BASE64_BASIC_AUTH)
                .queryParam(FidorConstants.BODY.OPENAPI.GRANT_TYPE, FidorConstants.BODY.OPENAPI.GRANT_TYPE_REFRESH_TOKEN)
                .queryParam(FidorConstants.BODY.OPENAPI.REFRESH_TOKEN, tokenEntity.getRefreshToken())
                .post(OpenTokenEntity.class);
    }

    public AccountResponse fetchOpenApiAccounts(OpenTokenEntity tokenEntity){

        String bearer = FidorConstants.BEARER_TOKEN + tokenEntity.getAccessToken();

        return getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE,
                FidorConstants.URL.OPENAPI.ACCOUNTS,
                bearer,
                MediaType.APPLICATION_JSON,
                FidorConstants.HEADERS.OPENAPI.APPLICATION_JSON_FIDOR_V1)
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchOpenApiTransactions(OpenTokenEntity tokenEntity, int page){

        String bearer = FidorConstants.BEARER_TOKEN + tokenEntity.getAccessToken();

        return getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE, FidorConstants.URL.OPENAPI.TRANSACTIONS,
                bearer,
                MediaType.APPLICATION_JSON,
                FidorConstants.HEADERS.OPENAPI.APPLICATION_JSON_FIDOR_V1)
                .queryParam(FidorConstants.BODY.OPENAPI.PAGE, Integer.toString(page))
                .queryParam(FidorConstants.BODY.OPENAPI.PER_PAGE, FidorConstants.BODY.OPENAPI.PER_PAGE_MAXVALUE)
                .get(TransactionResponse.class);
    }

    public UpcomingTransactionsResponse fetchUpcomingTransactions(OpenTokenEntity tokenEntity){
        String bearer = FidorConstants.BEARER_TOKEN + tokenEntity.getAccessToken();

         return getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE, FidorConstants.URL.OPENAPI.UPCOMING_TRANSACTIONS,
                 bearer,
                 MediaType.APPLICATION_JSON,
                 FidorConstants.HEADERS.OPENAPI.APPLICATION_JSON_FIDOR_V1)
                .get(UpcomingTransactionsResponse.class);
    }

    public OpenApiRateLimitEntity fetchOpenApiRateLimit(OpenTokenEntity tokenEntity){

        return getRequest(FidorConstants.URL.OPENAPI.SANDBOX_BASE, FidorConstants.URL.OPENAPI.RATELIMIT,
                tokenEntity.getAccessToken(),
                MediaType.APPLICATION_JSON,
                FidorConstants.HEADERS.OPENAPI.APPLICATION_JSON_FIDOR_V1)
                .get(OpenApiRateLimitEntity.class);
    }


}
