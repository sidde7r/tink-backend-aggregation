package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator;

import com.google.common.collect.Maps;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.LoginDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.NumpadDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage.BnpParibasPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class BnpParibasAuthenticator implements PasswordAuthenticator {
    private final BnpParibasApiClient apiClient;
    private final BnpParibasPersistentStorage bnpParibasPersistentStorage;
    private static final Logger log = LoggerFactory.getLogger(BnpParibasAuthenticator.class);

    public BnpParibasAuthenticator(
            BnpParibasApiClient apiClient,
            BnpParibasPersistentStorage bnpParibasPersistentStorage) {
        this.apiClient = apiClient;
        this.bnpParibasPersistentStorage = bnpParibasPersistentStorage;
    }

    @Override
    public void authenticate(String username, String password) throws LoginException {
        log.info("Enter authenticate()");
        if (!bnpParibasPersistentStorage.isRegisteredDevice()) {
            registerAndLogin(username, password);
        } else {
            login(bnpParibasPersistentStorage.getLoginId(), password);
        }
        log.info("Leave authenticate()");
    }

    private void registerAndLogin(String username, String password) throws LoginException {
        log.info("Enter registerAndLogin()");
        bnpParibasPersistentStorage.createAndSaveIdfaAndIdfvValues();

        LoginDataEntity loginData = login(username, password);

        bnpParibasPersistentStorage.saveLoginId(loginData.getLoginId());
        log.info("Leave registerAndLogin()");
    }

    private LoginDataEntity login(String username, String password) throws LoginException {
        log.info("Enter login()");
        if (!StringUtils.isNumeric(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        NumpadDataEntity numpadData = apiClient.getNumpadParams();
        String passwordIndices = getIndexStringFromPassword(numpadData.getGrid(), password);

        log.info("Leave login()");
        return apiClient.login(
                username, numpadData.getGridId(), passwordIndices, bnpParibasPersistentStorage);
    }

    private String getIndexStringFromPassword(String base64Image, String password) {
        log.info("Enter getIndexStringFromPassword()");
        String parsedDigits = ImageRecognizer.ocr(base64Image).replaceAll("\\s", "");
        assertCorrectNumpadSize(parsedDigits.length());

        Map<Character, String> indexByDigit = getIndexByDigitMap(parsedDigits);

        log.info("Leave getIndexStringFromPassword()");
        return buildIndexString(password, indexByDigit);
    }

    /**
     * The digits in the numpad are indexed ij, where i is 0 based and j is 1 based, i.e. 01, 02,
     * 03, 04, 05, 06, 07, 08, 09, 11
     */
    private Map<Character, String> getIndexByDigitMap(String parsedDigits) {
        log.info("Enter getIndexByDigitMap()");
        Map<Character, String> indexByDigit = Maps.newHashMap();

        for (int i = 0; i < BnpParibasConstants.Auth.NUMPAD_SIZE; i++) {
            Character digit = parsedDigits.charAt(i);
            assertNoDuplicateDigits(indexByDigit, digit);

            if (i == BnpParibasConstants.Auth.NUMPAD_SIZE - 1) {
                indexByDigit.put(
                        digit, BnpParibasConstants.Auth.INDEX_1 + BnpParibasConstants.Auth.INDEX_1);
                continue;
            }

            indexByDigit.put(digit, BnpParibasConstants.Auth.INDEX_0 + (i + 1));
        }

        log.info("Leave getIndexByDigitMap()");
        return indexByDigit;
    }

    private String buildIndexString(String password, Map<Character, String> indexByDigit) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < password.length(); i++) {
            Character digit = password.charAt(i);
            sb.append(indexByDigit.get(digit));
        }

        return sb.toString();
    }

    private void assertNoDuplicateDigits(Map<Character, String> indexByDigit, Character digit) {
        if (indexByDigit.containsKey(digit)) {
            throw new IllegalStateException(
                    "Couldn't parse numpad correctly. Found duplicate digits");
        }
    }

    private void assertCorrectNumpadSize(int numParsedDigits) {
        if (numParsedDigits != BnpParibasConstants.Auth.NUMPAD_SIZE) {
            throw new IllegalStateException(
                    String.format(
                            "Couldn't parse numpad correctly. Found %d digits, expected %d.",
                            numParsedDigits, BnpParibasConstants.Auth.NUMPAD_SIZE));
        }
    }
}
