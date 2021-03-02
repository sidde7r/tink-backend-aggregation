package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILinkingAndLoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.SignatureDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginResponse1;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginResponse2;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class EvoBancoMultifactorAuthenticator implements SmsOtpAuthenticatorPassword<String> {
    private final EvoBancoApiClient bankClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public EvoBancoMultifactorAuthenticator(
            EvoBancoApiClient bankClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.bankClient = bankClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public String init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        try {
            bankClient.login(new LoginRequest(username, password));
        } catch (HttpResponseException e) {
            if (e.getResponse()
                    .getBody(ErrorEntity.class)
                    .getMessage()
                    .equalsIgnoreCase(ErrorMessages.AUTHENTICATION_ERROR_MSG)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        try {
            String deviceId =
                    UUID.randomUUID()
                            .toString()
                            .toUpperCase()
                            .substring(0, EvoBancoConstants.Constants.DEVICE_ID_LENGTH);
            persistentStorage.put(EvoBancoConstants.Storage.DEVICE_ID, deviceId);

            SignatureDataEntity signatureDataEntity =
                    new SignatureDataEntity.Builder()
                            .withReferenceOtp("")
                            .withOtp("")
                            .withSignature(
                                    EvoBancoConstants.HardCodedValues.FIRST_LINKING_SIGNATURE)
                            .build();

            EeILinkingAndLoginEntity eeILinkingAndLoginEntity =
                    getEeILinkingAndLoginEntity(signatureDataEntity);

            LinkingLoginResponse1 linkingLoginResponse1 =
                    bankClient.link1(new LinkingLoginRequest(eeILinkingAndLoginEntity));

            return linkingLoginResponse1.getEeOLinkingAndLogin().getAnswer().getReferenceotp();
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                e.getResponse().getBody(LinkingLoginResponse1.class).handleReturnCode();
            }

            throw e;
        }
    }

    @Override
    public void authenticate(String otp, String referenceOtp)
            throws AuthenticationException, AuthorizationException {

        SignatureDataEntity signatureDataEntity =
                new SignatureDataEntity.Builder()
                        .withReferenceOtp(referenceOtp)
                        .withOtp(otp)
                        .withSignature(EvoBancoConstants.HardCodedValues.SECOND_LINKING_SIGNATURE)
                        .build();

        EeILinkingAndLoginEntity eeILinkingAndLoginEntity =
                getEeILinkingAndLoginEntity(signatureDataEntity);

        try {
            HttpResponse httpResponse =
                    bankClient.link2(new LinkingLoginRequest(eeILinkingAndLoginEntity));

            checkIfSecondFactorAuthenticationSucceded(httpResponse);

            sessionStorage.put(
                    EvoBancoConstants.Storage.INTERNAL_ID_PE,
                    httpResponse
                            .getBody(LinkingLoginResponse2.class)
                            .getEeOLinkingAndLogin()
                            .getAnswer()
                            .getInternalIdPe());

            // Workaround needed due to the fact that EvoBanco's backend expects a check of the
            // global
            // position (accounts and cards)
            // immediately after the eeLogin, keep alive requests will fail if this is not done
            // first,
            bankClient.globalPositionFirstTime().handleReturnCode();
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();
            if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                e.getResponse().getBody(LinkingLoginResponse2.class).handleReturnCode();
            }
            throw e;
        }
    }

    private void checkIfSecondFactorAuthenticationSucceded(HttpResponse httpResponse) {
        httpResponse
                .getBody(LinkingLoginResponse2.class)
                .getErrors()
                .ifPresent(
                        errorsEntity -> {
                            log.warn(
                                    "Issue with second factor authentication {}",
                                    errorsEntity.toString());
                            throw SupplementalInfoError.NO_VALID_CODE.exception();
                        });
    }

    private EeILinkingAndLoginEntity getEeILinkingAndLoginEntity(
            SignatureDataEntity signatureDataEntity) {
        return new EeILinkingAndLoginEntity.Builder()
                .withNic(credentials.getField(Field.Key.USERNAME))
                .withOperatingSystem(EvoBancoConstants.HardCodedValues.OPERATING_SYSTEM)
                .withSignatureData(signatureDataEntity)
                .withPassword(credentials.getField(Field.Key.PASSWORD))
                .withIdDevice(persistentStorage.get(EvoBancoConstants.Storage.DEVICE_ID))
                .withIdApp(EvoBancoConstants.HardCodedValues.APP_ID)
                .withVersionApp(EvoBancoConstants.HardCodedValues.APP_VERSION)
                .withMobileAccess(EvoBancoConstants.HardCodedValues.MOBILE_ACCESS)
                .withModel(EvoBancoConstants.HardCodedValues.MODEL)
                .withVersionApi(EvoBancoConstants.HardCodedValues.API_VERSION)
                .withEntityCode(sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                .build();
    }
}
