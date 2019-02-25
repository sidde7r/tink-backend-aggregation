package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.request.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.request.PutSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.response.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.response.PutRestSessionResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

public class IngApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public IngApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public CreateSessionResponse postLoginRestSession(String username, int usernameType, String dob) {

        LocalDate birthday = LocalDate.parse(dob, IngUtils.BIRTHDAY_INPUT);
        CreateSessionRequest request = CreateSessionRequest.create(username, usernameType, birthday);

        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(CreateSessionResponse.class, request);
    }

    public PutRestSessionResponse putLoginRestSession(List<Integer> pinPositions) {
        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(PutRestSessionResponse.class, PutSessionRequest.create(pinPositions));
    }


    public boolean postLoginAuthResponse(String ticket) {

        client.request(IngConstants.Url.LOGIN_AUTH_RESPONSE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(Form.builder()
                        .put(IngConstants.Form.TICKET, ticket)
                        .put(IngConstants.Form.DEVICE, IngConstants.Default.MOBILE_PHONE)
                        .build().serialize()
                );

        return true;
    }

    public String deleteApiRestSession() {
        return client.request(IngConstants.Url.API_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .delete(String.class);
    }
}
