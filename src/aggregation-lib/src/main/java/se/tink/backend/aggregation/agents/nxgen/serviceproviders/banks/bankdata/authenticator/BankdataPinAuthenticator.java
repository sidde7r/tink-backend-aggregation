package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.TimeTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BankdataPinAuthenticator implements PasswordAuthenticator {

    private final BankdataApiClient bankClient;

    public BankdataPinAuthenticator(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        try {
            TimeTokenResponse timeTokenResponse = this.bankClient.getTimeToken();
            String timeToken = timeTokenResponse.getTimeToken();
            LoginRequest loginRequest = new LoginRequest()
                    .setTimeToken(timeToken)
                    .setUserId(username)
                    .setPinCode(password)
                    .setLoginToken(calculateLoginToken(username, password, timeToken));
            this.bankClient.pinLogin(loginRequest);
        } catch (HttpResponseException e) {
            checkErrorForBankserviceOffline(e);
            throw e;
        }
    }

    private void checkErrorForBankserviceOffline(HttpResponseException e) throws BankServiceException {
        HttpResponse response = e.getResponse();
        if (response.hasBody()) {
            LoginErrorResponse errorResponse = response.getBody(LoginErrorResponse.class);
            if (errorResponse.getErrorCode() == BankdataConstants.Authentication.ERROR_CODE_BANK_SERVICE_OFFLINE) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
        }
    }

    private String calculateLoginToken(String userId, String pinCode, String timeToken) {
        try {
            String message = timeToken + userId + pinCode;
            SecretKeySpec keySpec = new SecretKeySpec(BankdataConstants.Authentication.LOGIN_SECRET.getBytes(),
                    BankdataConstants.Authentication.ALGORITHM);
            Mac sha256_HMAC = Mac.getInstance(BankdataConstants.Authentication.ALGORITHM);
            sha256_HMAC.init(keySpec);
            byte[] tokenBytes = sha256_HMAC.doFinal(message.getBytes());

            return Hex.encodeHexString(tokenBytes);
        } catch (Exception e) {
            throw new RuntimeException("Cannot calculate login token", e);
        }
    }
}
