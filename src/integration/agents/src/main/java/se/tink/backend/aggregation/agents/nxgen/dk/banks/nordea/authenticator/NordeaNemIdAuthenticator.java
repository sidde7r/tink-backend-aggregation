package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthenticatedUserEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthorizeAgreementDetails;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.NemidAuthenticateUserEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementRequestBody;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthorizeAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.InitialParametersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdAuthenticateUserRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdAuthenticatorV1;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdParametersV1;

public class NordeaNemIdAuthenticator implements NemIdAuthenticatorV1 {

    private final NordeaDkApiClient bankClient;
    private final NordeaDkSessionStorage sessionStorage;

    public NordeaNemIdAuthenticator(
            NordeaDkApiClient bankClient, NordeaDkSessionStorage sessionStorage) {

        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public NemIdParametersV1 getNemIdParameters() throws AuthenticationException {
        InitialParametersResponse initialParametersResponse = bankClient.fetchInitialParameters();
        sessionStorage.setSessionId(
                initialParametersResponse.getInitialParametersResponse().getSessionId());
        return new NemIdParametersV1(
                initialParametersResponse
                        .getInitialParametersResponse()
                        .getInitialParameters()
                        .getParamTable()
                        .getVal());
    }

    @Override
    public void exchangeNemIdToken(String nemIdToken)
            throws AuthenticationException, AuthorizationException {
        String sessionId = sessionStorage.getSessionId();

        NemIdAuthenticateUserRequestBody authenticateUserRequest =
                new NemIdAuthenticateUserRequestBody()
                        .setNemIdAuthenticateUserRequest(
                                new NemidAuthenticateUserEntity()
                                        .setLoginType(
                                                NordeaDkConstants.Authentication.LOGIN_TYPE_NEMID)
                                        .setNemIdSessionId(sessionId)
                                        .setNemIdToken(nemIdToken));

        AuthenticatedUserEntity authenticateUserResponse =
                bankClient
                        .nemIdAuthenticateUser(authenticateUserRequest)
                        .getAuthenticateUserResponse();

        Optional<String> errorCode = authenticateUserResponse.getErrorCode();

        if (errorCode.isPresent()) {
            if (errorCode.get().equals(ErrorCode.NO_ACCESS_TO_MOBILBANK)) {
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
            }

            if (ErrorCode.BANK_IS_DOWN_ERRORS_LIST.contains(errorCode.get())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Error code: " + errorCode.get());
            }

            throw new IllegalStateException(
                    NordeaV20Constants.GENERAL_ERROR_MESSAGES_BY_CODE.getOrDefault(
                            errorCode.get(), "Nordea ErrorCode: " + errorCode.get()));
        }

        sessionStorage.setToken(authenticateUserResponse.getAuthenticationToken().getToken());
        List<AgreementEntity> agreements = authenticateUserResponse.getAgreements();

        if (agreements.size() < 1) {
            throw new IllegalStateException("No agreements found");
        }
        // is it possible to have multiple agreements?
        String agreementId = agreements.get(0).getId();

        AuthorizeAgreementRequestBody authorizeAgreementRequest =
                new AuthorizeAgreementRequestBody()
                        .setAuthorizeAgreementRequest(
                                new AuthorizeAgreementDetails().setAgreement(agreementId));

        AuthorizeAgreementResponse authorizeAgreementResponse =
                bankClient.authorizeAgreement(authorizeAgreementRequest);
        String token =
                authorizeAgreementResponse
                        .getAuthorizeAgreementResponse()
                        .getAuthenticationToken()
                        .getToken();
        bankClient.setToken(token);
    }

    @Override
    public List<Cookie> getCookies() {
        return Collections.emptyList();
    }
}
