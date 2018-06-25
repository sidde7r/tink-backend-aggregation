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

        HttpResponse res = createRequest(LaCaixaConstants.Urls.INIT_LOGIN, request)
                .post(HttpResponse.class, request);

        return res.getBody(SessionResponse.class);
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {

            HttpResponse rawResponse = createRequest(LaCaixaConstants.Urls.SUBMIT_LOGIN, loginRequest)
                    .post(HttpResponse.class, loginRequest);

            return rawResponse.getBody(LoginResponse.class);

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

    private RequestBuilder createRequest(URL url, Object request){

        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
