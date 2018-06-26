package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity.LOGGER;

public class LaCaixaApiClient {

    private final TinkHttpClient client;

    public LaCaixaApiClient(TinkHttpClient client){

        this.client = client;
    }

    public SessionResponse initializeSession() {

        SessionRequest request = new SessionRequest(
                LaCaixaConstants.DefaultRequestParams.IDIOMA,
                LaCaixaConstants.DefaultRequestParams.ORIGEN,
                LaCaixaConstants.DefaultRequestParams.CANAL,
                LaCaixaConstants.DefaultRequestParams.ID_INSTALACION
        );

        return createRequest(LaCaixaConstants.Urls.INIT_LOGIN, request)
                .post(SessionResponse.class, request);
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {

            return createRequest(LaCaixaConstants.Urls.SUBMIT_LOGIN, loginRequest)
                    .post(LoginResponse.class, loginRequest);

        } catch (HttpResponseException e){
            int statusCode = e.getResponse().getStatus();

            switch (statusCode){

                case LaCaixaConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD:
                    LOGGER.trace("Login failed, incorrect username/password.");
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            LOGGER.warn("Login failed, status code: " + statusCode);
            throw e;
        }
    }

    public boolean isAlive(){

        try{

            createRequest(LaCaixaConstants.Urls.KEEP_ALIVE, null).get(HttpResponse.class);
            return true;
        } catch(HttpResponseException e){

            return false;
        }
    }

    private RequestBuilder createRequest(URL url, Object request){

        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
