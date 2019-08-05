package se.tink.backend.aggregation.nxgen.controllers.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SupplementalInformationController {
    private static final Logger logger =
            LoggerFactory.getLogger(SupplementalInformationController.class);

    private final SupplementalRequester supplementalRequester;
    private final Credentials credentials;

    public SupplementalInformationController(
            SupplementalRequester supplementalRequester, Credentials credentials) {
        this.supplementalRequester = supplementalRequester;
        this.credentials = credentials;
    }

    public Optional<Map<String, String>> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
        Optional<String> supplementalInformation =
                supplementalRequester.waitForSupplementalInformation(key, waitFor, unit);
        return supplementalInformation.map(
                s ->
                        SerializationUtils.deserializeFromString(
                                s, new TypeReference<HashMap<String, String>>() {}));
    }

    public Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        logger.info("Requesting supplemental information");
        String supplementalInformation =
                Optional.ofNullable(
                                Strings.emptyToNull(
                                        supplementalRequester.requestSupplementalInformation(
                                                credentials)))
                        .orElseThrow(SupplementalInfoError.NO_VALID_CODE::exception);
        logger.info("Finished requesting supplemental information");

        return Optional.ofNullable(
                        SerializationUtils.deserializeFromString(
                                supplementalInformation,
                                new TypeReference<HashMap<String, String>>() {}))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "SupplementalInformationResponse cannot be deserialized"));
    }

    public void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload) {
        Preconditions.checkNotNull(payload);

        credentials.setSupplementalInformation(SerializationUtils.serializeToString(payload));
        credentials.setStatus(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);

        final String deepLinkUrl =
                Optional.ofNullable(payload.getIos()).map(Ios::getDeepLinkUrl).orElse("<none>");

        logger.info("Opening third party app with deep link URL {}", deepLinkUrl);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }
}
