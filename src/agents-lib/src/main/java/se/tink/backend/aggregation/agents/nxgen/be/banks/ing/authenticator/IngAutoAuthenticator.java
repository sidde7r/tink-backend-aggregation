package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IngAutoAuthenticator implements AutoAuthenticator {
    private final IngApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final IngHelper ingHelper;

    public IngAutoAuthenticator(IngApiClient apiClient, PersistentStorage persistentStorage, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.ingHelper = ingHelper;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        MobileHelloResponseEntity mobileHelloResponseEntity = this.apiClient.mobileHello();
        this.ingHelper.addRequestUrls(mobileHelloResponseEntity.getRequests());

        int otp = calcOtp();

        String authUrl = this.ingHelper.getUrl(IngConstants.RequestNames.AUTHENTICATE);

        this.apiClient.trustBuilderLogin(
                authUrl,
                this.persistentStorage.get(IngConstants.Storage.ING_ID),
                this.ingHelper.getCardNumber(),
                otp,
                this.persistentStorage.get(IngConstants.Storage.DEVICE_ID),
                this.persistentStorage.get(IngConstants.Storage.PSN));

        String loginUrl = this.ingHelper.getUrl(IngConstants.RequestNames.LOGON);

        HttpResponse LoginHttpResponse = this.apiClient.autoLogin(loginUrl,
                this.persistentStorage.get(IngConstants.Storage.ING_ID),
                this.ingHelper.getCardNumber(),
                this.persistentStorage.get(IngConstants.Storage.DEVICE_ID));
        checkAutoLoginError(LoginHttpResponse);

        LoginResponse loginResponse = LoginHttpResponse.getBody(LoginResponse.class);
        LoginResponseEntity loginResponseEntity = loginResponse.getMobileResponse();

        this.ingHelper.persist(loginResponseEntity);
    }

    private void checkAutoLoginError(HttpResponse httpResponse) throws SessionException {
        // We don't get a specific error msg if autologin doesn't work,
        // just a 302 (that goes to an old page) when we try to auto-login.
        if (httpResponse.getStatus() == HttpStatusCodes.STATUS_CODE_FOUND) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private int calcOtp() {
        String storageOtp = this.persistentStorage.get(IngConstants.Storage.OTP_COUNTER);
        int otpCounter = Integer.parseInt(storageOtp);
        byte[] otpKey = EncodingUtils.decodeHexString(this.persistentStorage.get(IngConstants.Storage.OTP_KEY_HEX));
        int otp = IngCryptoUtils.calculateOtpForAuthentication(otpKey, otpCounter);
        otpCounter++;
        this.persistentStorage.put(IngConstants.Storage.OTP_COUNTER, otpCounter);

        return otp;
    }
}
