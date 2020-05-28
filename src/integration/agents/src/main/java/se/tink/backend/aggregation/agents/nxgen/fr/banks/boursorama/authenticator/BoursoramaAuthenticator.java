package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.GenerateMatrixResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.storage.BoursoramaPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class BoursoramaAuthenticator implements PasswordAuthenticator {
    private final BoursoramaApiClient apiClient;
    private BoursoramaPersistentStorage boursoramaPersistentStorage;

    public BoursoramaAuthenticator(
            BoursoramaApiClient apiClient,
            BoursoramaPersistentStorage boursoramaPersistentStorage) {
        this.apiClient = apiClient;
        this.boursoramaPersistentStorage = boursoramaPersistentStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        GenerateMatrixResponse generateMatrixResponse = apiClient.generateMatrix();
        BoursoramaImageMapper imageMapper =
                new BoursoramaImageMapper(generateMatrixResponse.getKeys());
        if (!StringUtils.isNumeric(username) || !StringUtils.isNumeric(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        String numpadKeysPassword =
                Arrays.stream(password.split(""))
                        .map(Integer::parseInt)
                        .map(imageMapper::getPinCharacterRepresentation)
                        .collect(Collectors.joining("|"));

        String matrixRandomChallenge = generateMatrixResponse.getMatrixRandomChallenge();

        // +[BRSSettings getUUID], checked to be random.
        String udid = boursoramaPersistentStorage.getUdid();
        if (Strings.isNullOrEmpty(udid)) {
            udid = UUID.randomUUID().toString();
            boursoramaPersistentStorage.saveUdid(udid);
        }

        String deviceEnrolmentTokenValue =
                boursoramaPersistentStorage.getDeviceEnrolmentTokenValue();
        boolean firstLogin = Strings.isNullOrEmpty(deviceEnrolmentTokenValue);

        LoginRequest loginRequest;
        if (firstLogin) {
            loginRequest =
                    LoginRequest.createFirstLogin(
                            BoursoramaConstants.Auth.FIRST_AUTH,
                            username,
                            matrixRandomChallenge,
                            numpadKeysPassword,
                            udid);
        } else {
            loginRequest =
                    LoginRequest.createRepeatedDeviceLogin(
                            username,
                            matrixRandomChallenge,
                            numpadKeysPassword,
                            udid,
                            deviceEnrolmentTokenValue);
        }

        apiClient.login(loginRequest);
    }
}
