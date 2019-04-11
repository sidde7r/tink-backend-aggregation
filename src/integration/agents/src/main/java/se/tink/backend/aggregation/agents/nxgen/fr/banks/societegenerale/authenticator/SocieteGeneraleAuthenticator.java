package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.AuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.LoginGridData;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class SocieteGeneraleAuthenticator implements Authenticator {

    private final SocieteGeneraleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SocieteGeneraleAuthenticator(
            SocieteGeneraleApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        Optional<LoginGridData> gridResponse = apiClient.getLoginGrid();

        if (!gridResponse.isPresent()) {
            throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception();
        }

        LoginGridData gridData = gridResponse.get();
        List<Integer> oneTimePad = gridData.getOneTimePad();
        String crypto = gridData.getCrypto();

        byte[] numberPad = apiClient.getLoginNumPadImage(crypto);

        String encryptedPasscode = getEncryptedPasscode(password, numberPad, oneTimePad);

        Optional<AuthenticationData> resp =
                apiClient.postAuthentication(username, crypto, encryptedPasscode);

        if (!resp.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else {
            persistentStorage.put(SocieteGeneraleConstants.StorageKey.TOKEN, resp.get().getToken());
            sessionStorage.put(
                    SocieteGeneraleConstants.StorageKey.SESSION_KEY, resp.get().getSessionKey());
        }
    }

    private String getEncryptedPasscode(
            String passCode, byte[] numberPad, List<Integer> oneTimePad) {

        String parsedDigits = ImageRecognizer.ocr(numberPad, Color.WHITE).replaceAll("\\s", "");
        if (parsedDigits.matches("[0-9]{10}]")) {
            throw new IllegalStateException(
                    String.format(
                            "Couldn't parse 10 digits from shuffled number pad: %s", parsedDigits));
        }

        ArrayList<Integer> indices =
                new ArrayList<>(Collections.nCopies(parsedDigits.length(), -1));
        for (int i = 0; i < parsedDigits.length(); i++) {
            int digit = Integer.parseInt(parsedDigits.substring(i, i + 1));
            if (indices.get(digit) > -1) {
                throw new RuntimeException(
                        String.format(
                                "Couldn't parse number pad correctly, got duplicate digits: %s",
                                parsedDigits));
            }
            indices.set(digit, i);
        }

        int[] encryptedPasscode = new int[passCode.length()];
        for (int i = 0; i < passCode.length(); i++) {
            int digit = Integer.parseInt(passCode.substring(i, i + 1));
            int index = indices.get(digit);
            encryptedPasscode[i] = oneTimePad.get(10 * i + index);
        }

        return Arrays.stream(encryptedPasscode)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
