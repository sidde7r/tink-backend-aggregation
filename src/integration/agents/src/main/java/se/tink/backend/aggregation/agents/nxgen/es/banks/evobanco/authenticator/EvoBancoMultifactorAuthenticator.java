package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILinkingAndLoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.SignatureDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginResponse1;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        String deviceId =
                UUID.randomUUID()
                        .toString()
                        .toUpperCase()
                        .substring(0, EvoBancoConstants.Constants.DEVICE_ID_LENGTH);
        persistentStorage.put(EvoBancoConstants.Storage.DEVICE_ID, deviceId);

        Map<String, String> initValues = new HashMap<>();
        // Construct login request from username and hashed password
        bankClient.login(new LoginRequest(username, password));

        SignatureDataEntity signatureDataEntity =
                new SignatureDataEntity.Builder()
                        .withReferenceOtp("")
                        .withOtp("")
                        .withSignature(EvoBancoConstants.HardCodedValues.FIRST_LINKING_SIGNATURE)
                        .build();

        EeILinkingAndLoginEntity eeILinkingAndLoginEntity =
                getEeILinkingAndLoginEntity(signatureDataEntity);

        LinkingLoginResponse1 linkingLoginResponse1 =
                bankClient.link1(new LinkingLoginRequest(eeILinkingAndLoginEntity));

        return linkingLoginResponse1.getEeOLinkingAndLogin().getAnswer().getReferenceotp();
    }

    @Override
    public void authenticate(String otp, String initValues)
            throws AuthenticationException, AuthorizationException {

        SignatureDataEntity signatureDataEntity =
                new SignatureDataEntity.Builder()
                        .withReferenceOtp(initValues)
                        .withOtp(otp)
                        .withSignature(EvoBancoConstants.HardCodedValues.SECOND_LINKING_SIGNATURE)
                        .build();

        EeILinkingAndLoginEntity eeILinkingAndLoginEntity =
                getEeILinkingAndLoginEntity(signatureDataEntity);

        bankClient.link2(new LinkingLoginRequest(eeILinkingAndLoginEntity));

        // Workaround needed due to the fact that EvoBanco's backend expects a check of the global
        // position (accounts and cards)
        // immediately after the eeLogin, keep alive requests will fail if this is not done first,
        bankClient.globalPositionFirstTime();
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
