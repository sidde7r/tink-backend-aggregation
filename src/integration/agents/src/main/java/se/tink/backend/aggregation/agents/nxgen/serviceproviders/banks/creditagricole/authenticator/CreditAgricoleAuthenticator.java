package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.SignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.StrongAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.ErrorEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class CreditAgricoleAuthenticator implements Authenticator {

    private final AggregationLogger log = new AggregationLogger(CreditAgricoleAuthenticator.class);
    private final CreditAgricoleApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public CreditAgricoleAuthenticator(
            CreditAgricoleApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String userAccountNumber = credentials.getField(StorageKey.USER_ACCOUNT_NUMBER);
        String userAccountCode = credentials.getField(StorageKey.USER_ACCOUNT_CODE);
        String appCode = credentials.getField(StorageKey.APP_CODE);

        String shuffledAccountCode = getShuffledAccountCode(apiClient.numberPad(), userAccountCode);
        SignInResponse signInResponse =
                checkResponseErrors(apiClient.signIn(userAccountNumber, shuffledAccountCode));

        // At this point we have authenticated the customer, and are safe to put details in
        // persistent storage
        persistentStorage.put(StorageKey.USER_ACCOUNT_NUMBER, userAccountNumber);
        persistentStorage.put(StorageKey.USER_ACCOUNT_CODE, userAccountCode);
        persistentStorage.put(StorageKey.APP_CODE, appCode);
        sessionStorage.put(StorageKey.SHUFFLED_USER_ACCOUNT_CODE, shuffledAccountCode);

        String partnerId = signInResponse.getActiveUsersList().get(0).getPartnerId();
        persistentStorage.put(StorageKey.PARTNER_ID, partnerId);

        String userId = signInResponse.getActiveUsersList().get(0).getUserId();
        persistentStorage.put(StorageKey.USER_ID, userId);

        String loginEmail = signInResponse.getEmailPart();
        persistentStorage.put(StorageKey.LOGIN_EMAIL, loginEmail);

        checkResponseErrors(apiClient.appCode(appCode));

        StrongAuthenticationResponse strongAuthenticationResponse =
                checkResponseErrors(apiClient.strongAuthentication());
        String llToken = strongAuthenticationResponse.getLlToken();
        sessionStorage.put(StorageKey.LL_TOKEN, llToken);
    }

    /* HELPER METHODS */

    private <T extends DefaultResponse> T checkResponseErrors(T response) throws LoginException {
        if (response.getErrors().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ErrorEntity e : response.getErrors()) {
                if (e.getCode().equalsIgnoreCase(ErrorCode.INCORRECT_CREDENTIALS)) {
                    throw new LoginException(LoginError.INCORRECT_CREDENTIALS);
                }
                sb.append(e.toString());
            }

            throw new RuntimeException(sb.toString());
        }
        return response;
    }

    private String getShuffledAccountCode(byte[] numberPadBytes, String accountCode) {

        int digits = 10;
        String parsedDigits =
                ImageRecognizer.ocr(numberPadBytes, Color.WHITE).replaceAll("\\s", "");

        if (parsedDigits.length() != digits) {
            throw new RuntimeException("Couldn't parse numerical pad correctly.");
        }

        ArrayList<Integer> indices = new ArrayList<>(Collections.nCopies(digits, -1));
        for (int i = 0; i < parsedDigits.length(); i++) {
            int digit = Integer.parseInt(parsedDigits.substring(i, i + 1));
            if (indices.get(digit) > -1) {
                throw new RuntimeException("Couldn't parse numerical pad correctly.");
            }
            indices.set(digit, i);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < accountCode.length(); i++) {
            int digit = Integer.parseInt(accountCode.substring(i, i + 1));
            sb.append(indices.get(digit));
        }

        return sb.toString();
    }
}
