package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import com.google.common.base.Strings;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;

@Slf4j
@RequiredArgsConstructor
public class BecInitializeAuthenticationStep {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("\\d{10,11}");
    private static final Pattern MOBILECODE_PATTERN = Pattern.compile("\\d{4}");

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;

    public List<String> initAuthenticationAndFetch2FAOptions() {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        auditCredentials(username, password);

        syncAppDetails();

        String deviceId = generateDeviceId();
        storage.saveDeviceId(deviceId);

        return fetchScaOptions(username, password, deviceId);
    }

    private void auditCredentials(String username, String password) {
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        log.info(
                "{} Username matches pattern: {} ",
                BEC_LOG_TAG,
                USERNAME_PATTERN.matcher(username).matches());
        log.info(
                "{} Password matches pattern: {} ",
                BEC_LOG_TAG,
                MOBILECODE_PATTERN.matcher(password).matches());
    }

    private void syncAppDetails() {
        apiClient.appSync();
    }

    private List<String> fetchScaOptions(String username, String password, String deviceId) {
        return apiClient.getScaOptions(username, password, deviceId).getSecondFactorOptions();
    }

    private static String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
}
