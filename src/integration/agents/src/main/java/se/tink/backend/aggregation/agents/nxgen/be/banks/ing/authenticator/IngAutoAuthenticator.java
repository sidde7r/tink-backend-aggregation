package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import joptsimple.internal.Strings;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IngAutoAuthenticator implements AutoAuthenticator {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(IngAutoAuthenticator.class);
    private static final int MAXIMUM_TRY = 3;
    private final IngApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final IngHelper ingHelper;
    private int tryCounter;

    public IngAutoAuthenticator(
            IngApiClient apiClient, PersistentStorage persistentStorage, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.ingHelper = ingHelper;
        this.tryCounter = 0;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {

        MobileHelloResponseEntity mobileHelloResponseEntity = this.apiClient.mobileHello();
        this.ingHelper.addRequestUrls(mobileHelloResponseEntity.getRequests());

        String authUrl = this.ingHelper.getUrl(IngConstants.RequestNames.AUTHENTICATE);

        HttpResponse response = trustBuilderLoginWithOtp(authUrl);
        // xiacheng, sometimes the otp counter is not overwritten by previous call
        // here is the recovery mode.
        while (wrongOtp(response) && this.tryCounter++ < MAXIMUM_TRY) {
            response = trustBuilderLoginWithOtp(authUrl);
        }

        validateBuilderLoginResponse(response);

        String loginUrl = this.ingHelper.getUrl(IngConstants.RequestNames.LOGON);

        LoginResponseEntity loginResponseEntity =
                this.apiClient.login(
                        loginUrl,
                        this.persistentStorage.get(IngConstants.Storage.ING_ID),
                        this.persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER),
                        this.persistentStorage.get(IngConstants.Storage.DEVICE_ID));

        if (IngConstants.ReturnCodes.NOK.equalsIgnoreCase(loginResponseEntity.getReturnCode())) {
            throw new IllegalStateException(
                    String.format(
                            "%s%s%s%s",
                            "AutoAuth not successful! Code: ",
                            loginResponseEntity.getErrorCode().orElse("No error code"),
                            " Message: ",
                            loginResponseEntity.getErrorText()));
        }

        this.ingHelper.persist(loginResponseEntity);
    }

    private void validateBuilderLoginResponse(HttpResponse response) throws SessionException {
        String locationHeader = response.getHeaders().getFirst(IngConstants.Headers.LOCATION);
        if (!Strings.isNullOrEmpty(locationHeader)
                && locationHeader.toLowerCase().contains(IngConstants.Headers.TAM_ERROR)
                && (locationHeader.toLowerCase().contains(IngConstants.Headers.ERROR_CODE_WRONG_OTP)
                        || locationHeader
                                .toLowerCase()
                                .contains(IngConstants.Headers.ERROR_CODE_LOGIN))) {
            LOGGER.warn(
                    String.format(
                            "%s: %s",
                            IngConstants.LogMessage.UNKNOWN_LOCATION_CODE,
                            locationHeader.toLowerCase()));
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean wrongOtp(HttpResponse response) {
        String locationHeader = response.getHeaders().getFirst(IngConstants.Headers.LOCATION);
        if (Strings.isNullOrEmpty(locationHeader)) {
            return false;
        }
        if (!locationHeader.toLowerCase().contains(IngConstants.Headers.TAM_ERROR)
                && locationHeader
                        .toLowerCase()
                        .contains(IngConstants.Headers.ERROR_CODE_WRONG_OTP)) {
            LOGGER.warn(
                    String.format(
                            "%s: %s",
                            IngConstants.LogMessage.UNKNOWN_LOCATION_CODE,
                            locationHeader.toLowerCase()));
        }

        return locationHeader.toLowerCase().contains(IngConstants.Headers.ERROR_CODE_WRONG_OTP);
    }

    private HttpResponse trustBuilderLoginWithOtp(String authUrl) {
        int otp = calcOtp();
        return this.apiClient.trustBuilderLogin(
                authUrl,
                this.persistentStorage.get(IngConstants.Storage.ING_ID),
                this.persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER),
                otp,
                this.persistentStorage.get(IngConstants.Storage.DEVICE_ID),
                this.persistentStorage.get(IngConstants.Storage.PSN));
    }

    private int calcOtp() {
        String storageOtp = this.persistentStorage.get(IngConstants.Storage.OTP_COUNTER);
        int otpCounter = Integer.parseInt(storageOtp);
        byte[] otpKey =
                EncodingUtils.decodeHexString(
                        this.persistentStorage.get(IngConstants.Storage.OTP_KEY_HEX));
        int otp = IngCryptoUtils.calculateOtpForAuthentication(otpKey, otpCounter);
        otpCounter++;
        this.persistentStorage.put(IngConstants.Storage.OTP_COUNTER, otpCounter);

        return otp;
    }
}
