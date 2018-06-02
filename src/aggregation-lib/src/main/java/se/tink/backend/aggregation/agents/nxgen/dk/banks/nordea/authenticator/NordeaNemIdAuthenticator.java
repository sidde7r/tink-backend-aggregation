package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AgreementListEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthorizeAgreementDetails;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.NemidAuthenticateUserEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.InitialParametersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdAuthenticateUserRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemidAuthenticateUserResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdParameters;

public class NordeaNemIdAuthenticator implements NemIdAuthenticator {

    private final NordeaDkApiClient bankClient;
    private final NordeaDkSessionStorage sessionStorage;

    public NordeaNemIdAuthenticator(NordeaDkApiClient bankClient, NordeaDkSessionStorage sessionStorage) {

        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public NemIdParameters getNemIdParameters() throws AuthenticationException {
        InitialParametersResponse initialParametersResponse = bankClient.fetchInitialParameters();
        sessionStorage.setSessionId(initialParametersResponse.getInitialParametersResponse().getSessionId());
        return new NemIdParameters(
                initialParametersResponse.getInitialParametersResponse().getInitialParameters().getParamTable().getVal()
        );
    }

    @Override
    public void exchangeNemIdToken(String nemIdToken) throws AuthenticationException, AuthorizationException {
        String sessionId = sessionStorage.getSessionId();

        NemIdAuthenticateUserRequestBody authenticateUserRequest = new NemIdAuthenticateUserRequestBody()
                .setNemIdAuthenticateUserRequest(
                        new NemidAuthenticateUserEntity()
                        .setLoginType(NordeaDkConstants.Authentication.LOGIN_TYPE_NEMID)
                        .setNemIdSessionId(sessionId)
                        .setNemIdToken(nemIdToken)
                );

        NemidAuthenticateUserResponse authenticateUserResponse = bankClient.nemIdAuthenticateUser(authenticateUserRequest);
        sessionStorage.setToken(authenticateUserResponse.getAuthenticateUserResponse().getAuthenticationToken().getToken());
        List<AgreementListEntity> agreements = authenticateUserResponse.getAuthenticateUserResponse().getAgreements();

        if (agreements.size() < 1) {
            throw new IllegalStateException("No agreements found");
        }
        // is it possible to have multiple agreements?
        String agreementId = agreements.get(0).getAgreement().getId();

        AuthorizeAgreementRequestBody authorizeAgreementRequest = new AuthorizeAgreementRequestBody()
                .setAuthorizeAgreementRequest(
                        new AuthorizeAgreementDetails()
                                .setAgreement(agreementId)
                );

        AuthorizeAgreementResponse authorizeAgreementResponse = bankClient.authorizeAgreement(authorizeAgreementRequest);
        String token = authorizeAgreementResponse.getAuthorizeAgreementResponse().getAuthenticationToken().getToken();
        bankClient.setToken(token);
    }
}
