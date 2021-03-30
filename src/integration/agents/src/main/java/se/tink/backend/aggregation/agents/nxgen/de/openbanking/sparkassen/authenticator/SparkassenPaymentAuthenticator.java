package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;

public class SparkassenPaymentAuthenticator extends SparkassenAuthenticator
        implements PaymentAuthenticator {
    private SparkassenApiClient apiClient;

    public SparkassenPaymentAuthenticator(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SparkassenApiClient apiClient,
            SparkassenStorage sparkassenStorage,
            Credentials credentials) {
        super(catalog, supplementalInformationHelper, apiClient, sparkassenStorage, credentials);
        this.apiClient = apiClient;
    }

    public void authenticatePayment(
            Credentials credentials, CreatePaymentResponse createPaymentResponse)
            throws AuthenticationException, AuthorizationException {
        validateInput(credentials);
        logDetails(credentials);

        AuthenticationMethodResponse initAuthorizationResponse =
                initializeAuthorizationOfPayment(
                        createPaymentResponse,
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        AuthenticationMethodResponse scaMethodDetails =
                getPaymentScaMethodDetails(initAuthorizationResponse);

        authorizePaymentWithOtp(
                initAuthorizationResponse.getLinks().getSelectAuthenticationMethod().getHref(),
                scaMethodDetails.getChosenScaMethod(),
                scaMethodDetails.getChallengeData());
    }

    private AuthenticationMethodResponse initializeAuthorizationOfPayment(
            CreatePaymentResponse createPaymentResponse, String username, String password)
            throws AuthenticationException {

        URL url =
                Optional.ofNullable(createPaymentResponse.getLinks())
                        .map(LinksEntity::getStartAuthorisationWithPsuAuthentication)
                        .map(Href::getHref)
                        .map(URL::new)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SparkassenConstants.ErrorMessages
                                                        .MISSING_SCA_AUTHORIZATION_URL));
        return apiClient.initializeAuthorization(url, username, password);
    }

    private AuthenticationMethodResponse getPaymentScaMethodDetails(
            AuthenticationMethodResponse initAuthorizationResponse)
            throws SupplementalInfoException, LoginException {
        switch (initAuthorizationResponse.getScaStatus()) {
            case PSU_AUTHENTICATED:
                return getPaymentScaMethodDetailsOutOfMultiplePossible(
                        getSupportedScaMethods(initAuthorizationResponse),
                        initAuthorizationResponse
                                .getLinks()
                                .getSelectAuthenticationMethod()
                                .getHref());
            case SCA_METHOD_SELECTED:
                return initAuthorizationResponse;
            default:
                throw new IllegalStateException(
                        SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        }
    }

    private AuthenticationMethodResponse getPaymentScaMethodDetailsOutOfMultiplePossible(
            List<ScaMethodEntity> scaMethods, String authorizationUrl)
            throws SupplementalInfoException {
        ScaMethodEntity chosenScaMethod = collectScaMethod(scaMethods);
        AuthenticationMethodResponse authenticationMethodResponse =
                apiClient.selectPaymentAuthorizationMethod(
                        authorizationUrl, chosenScaMethod.getAuthenticationMethodId());

        if (authenticationMethodResponse.getChallengeData() == null) {
            throw new IllegalStateException(
                    SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        } else {
            return authenticationMethodResponse;
        }
    }

    private void authorizePaymentWithOtp(
            String transactionAuthorizationUrl,
            ScaMethodEntity scaMethod,
            ChallengeDataEntity challengeData)
            throws AuthenticationException {
        String otp = collectOtp(scaMethod, challengeData);
        FinalizeAuthorizationResponse finalizeAuthorizationResponse =
                apiClient.finalizePaymentAuthorization(transactionAuthorizationUrl, otp);

        switch (finalizeAuthorizationResponse.getScaStatus()) {
            case FINALISED:
                break;
            case FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }
}
