package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.InitialParametersRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.InitialParametersRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.InitialParametersRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.InitialParametersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdAuthenticateUserRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdAuthenticateUserRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemidAuthenticateUserResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class NordeaDkApiClient extends NordeaV20ApiClient {
    private final NordeaDkSessionStorage sessionStorage;

    public NordeaDkApiClient(
            NordeaDkSessionStorage sessionStorage,
            TinkHttpClient client,
            Credentials credentials,
            String marketCode) {
        super(client, credentials, marketCode);

        this.sessionStorage = sessionStorage;
    }

    public InitialParametersResponse fetchInitialParameters() {
        InitialParametersRequestBody requestBody =
                new InitialParametersRequestBody()
                        .setInitialParametersRequest(
                                new InitialParametersRequestEntity()
                                        .setAuthLevel(
                                                NordeaDkConstants.Authentication.DEFAULT_AUTH_LEVEL)
                                        .setRemeberUserId(""));

        return request(new InitialParametersRequest(requestBody), InitialParametersResponse.class);
    }

    public NemidAuthenticateUserResponse nemIdAuthenticateUser(
            NemIdAuthenticateUserRequestBody requestBody)
            throws AuthorizationException, AuthenticationException {
        NemIdAuthenticateUserRequest request = new NemIdAuthenticateUserRequest(requestBody);

        return authRequest(request, NemidAuthenticateUserResponse.class);
    }

    public AuthorizeAgreementResponse authorizeAgreement(AuthorizeAgreementRequestBody requestBody)
            throws AuthorizationException, AuthenticationException {
        String token = sessionStorage.getToken();

        AuthorizeAgreementRequest request = new AuthorizeAgreementRequest(requestBody);
        request.getHeaders().add(NordeaV20Constants.HeaderKey.SECURITY_TOKEN, token);

        return authRequest(request, AuthorizeAgreementResponse.class);
    }
}
