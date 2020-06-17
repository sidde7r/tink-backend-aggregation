package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BecAuthenticator extends StatelessProgressiveAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(BecAuthenticator.class);

    static final String USERNAME_STORAGE_KEY = "username";
    static final String PASSWORD_STORAGE_KEY = "password";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("\\d{11}");
    private static final Pattern MOBILECODE_PATTERN = Pattern.compile("\\d{4}");

    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalRequester supplementalRequester;

    public BecAuthenticator(
            BecApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return ImmutableList.of(
                new AutomaticAuthenticationStep(this::syncAppDetails, "syncApp"),
                new UsernamePasswordAuthenticationStep(this::fetchScaOptions),
                new CombinedNemIdAuthenticationStep(
                        sessionStorage, apiClient, supplementalRequester));
    }

    private AuthenticationStepResponse syncAppDetails() {
        apiClient.appSync();
        return AuthenticationStepResponse.executeNextStep();
    }

    private void fetchScaOptions(String username, String password)
            throws LoginException, NemIdException {
        auditCredentials(username, password);
        apiClient.scaPrepare(username, password);
        sessionStorage.put(USERNAME_STORAGE_KEY, username);
        sessionStorage.put(PASSWORD_STORAGE_KEY, password);
    }

    private void auditCredentials(String username, String password) {
        log.info("Username matches pattern: {} ", USERNAME_PATTERN.matcher(username).matches());
        log.info("Password matches pattern: {} ", MOBILECODE_PATTERN.matcher(password).matches());
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
