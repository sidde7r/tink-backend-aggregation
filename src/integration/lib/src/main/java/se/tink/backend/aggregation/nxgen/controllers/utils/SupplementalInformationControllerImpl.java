package se.tink.backend.aggregation.nxgen.controllers.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.masker.StringMasker;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SupplementalInformationControllerImpl implements SupplementalInformationController {
    private static final Logger logger =
            LoggerFactory.getLogger(SupplementalInformationControllerImpl.class);

    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private static final int TIMEOUT_MINUTES_EMBEDDED_FIELDS = 5;

    private final SupplementalRequester supplementalRequester;
    private final Credentials credentials;
    private final String state;
    private final String initiator;

    /**
     * Do not construct your own SupplementalInfomationController. Use the instance available to
     * your agent from SubsequentGenerationAgent instead. Or even better, migrate to
     * AgentPlatformAgent or SubsequentProgressiveGenerationAgent where the Supplemental information
     * controlling is outside of the agent and you do not need to have an instance.
     */
    public SupplementalInformationControllerImpl(
            SupplementalRequester supplementalRequester,
            Credentials credentials,
            String state,
            String initiator) {
        this.supplementalRequester = supplementalRequester;
        this.credentials = credentials;
        this.state = state;
        this.initiator = initiator;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {

        Optional<String> result =
                supplementalRequester.waitForSupplementalInformation(
                        mfaId, waitFor, unit, initiator);

        if (!result.isPresent() || Strings.isNullOrEmpty(result.get())) {
            return Optional.empty();
        }

        return Optional.ofNullable(stringToMap(result.get()));
    }

    @Override
    public Map<String, String> askSupplementalInformationSync(Field... fields)
            throws SupplementalInfoException {

        String mfaId = askSupplementalInformationAsync(fields);

        String results =
                supplementalRequester
                        .waitForSupplementalInformation(
                                mfaId, TIMEOUT_MINUTES_EMBEDDED_FIELDS, TimeUnit.MINUTES, initiator)
                        .orElse(null);

        logSupplementalInfoResponse(results);

        String supplementalInformation =
                Optional.ofNullable(Strings.emptyToNull(results))
                        .orElseThrow(SupplementalInfoError.NO_VALID_CODE::exception);

        Map<String, String> suplementalInformation =
                deserializeSupplementalInformation(supplementalInformation);
        logger.info("Finished requesting supplemental information");
        suplementalInformation.forEach(this::logSupplementalInformation);
        return suplementalInformation;
    }

    private void logSupplementalInfoResponse(String supplementalResponse) {
        try {
            String masked = maskSupplementalInfoResponse(supplementalResponse);
            logger.info("Supplemental info response: [{}]", masked);
        } catch (Exception e) {
            logger.warn("Could not log supplemental info response", e);
        }
    }

    private String maskSupplementalInfoResponse(String supplementalResponse) {
        if (Strings.isNullOrEmpty(supplementalResponse)) {
            return supplementalResponse;
        }

        Map<String, String> supplementalResponseMap =
                deserializeSupplementalInformation(supplementalResponse);

        for (Map.Entry<String, String> entry : supplementalResponseMap.entrySet()) {
            String key = entry.getKey();
            String value = maskAllCharactersAfterIndex(entry.getValue(), 3);

            supplementalResponseMap.put(key, value);
        }
        return SerializationUtils.serializeToString(supplementalResponseMap);
    }

    @SuppressWarnings("SameParameterValue")
    private String maskAllCharactersAfterIndex(String value, int index) {
        if (Strings.isNullOrEmpty(value)) {
            return value;
        }
        if (index >= value.length()) {
            return value;
        }
        String unescaped = value.substring(0, index);
        String escaped = Strings.repeat("*", value.substring(index).length());
        return unescaped + escaped;
    }

    private String loggableSupplementalInformationKey(String key) {
        if (key == null) {
            return null;
        }
        // avoid logging account numbers during opt-in
        // if key has 6 or more digits, mask it
        final int numberOfDigits = key.replaceAll("\\D+", "").length();
        if (numberOfDigits >= 6) {
            return StringMasker.starMaskBeginningOfString(key);
        }
        return key;
    }

    private void logSupplementalInformation(String key, String value) {
        final String loggableKey = loggableSupplementalInformationKey(key);
        final String message;
        if (value == null) {
            message = "equals null";
        } else if (value.equalsIgnoreCase("true")) {
            message = "is true";
        } else if (value.equalsIgnoreCase("false")) {
            message = "is false";
        } else {
            message = "has length " + value.length();
        }

        logger.info("supplemental information {} {}", loggableKey, message);
    }

    @Override
    public String askSupplementalInformationAsync(Field... fields) {
        if (fields == null || fields.length == 0) {
            throw new IllegalStateException("Requires non-null, non-empty, list of fields");
        }

        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setStatusPayload(null);

        String names =
                Arrays.stream(fields)
                        .map(Field::getName)
                        .map(this::loggableSupplementalInformationKey)
                        .collect(Collectors.joining(","));
        logger.info("Requesting for fields: {}", names);

        // in case of embedded supplemental information, we use credentialsId as mfaId
        String mfaId = credentials.getId();
        supplementalRequester.requestSupplementalInformation(mfaId, credentials);

        return mfaId;
    }

    @Override
    public Optional<Map<String, String>> openThirdPartyAppSync(
            ThirdPartyAppAuthenticationPayload payload) {

        String mfaId = openThirdPartyAppAsync(payload);

        return waitForSupplementalInformation(
                mfaId, ThirdPartyAppConstants.WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public String openThirdPartyAppAsync(ThirdPartyAppAuthenticationPayload payload) {
        Preconditions.checkNotNull(payload);

        payload.setState(state);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(payload));
        credentials.setStatus(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);
        credentials.setStatusPayload(null);

        final String deepLinkUrl =
                Optional.ofNullable(payload.getIos()).map(Ios::getDeepLinkUrl).orElse("<none>");

        logger.info("Opening third party app with deep link URL {}, state {}", deepLinkUrl, state);

        // return the mfaId that can be listened for.
        String mfaId = String.format(UNIQUE_PREFIX_TPCB, this.state);
        supplementalRequester.requestSupplementalInformation(mfaId, credentials);

        return mfaId;
    }

    @Override
    public String openMobileBankIdAsync(String autoStartToken) {
        credentials.setSupplementalInformation(autoStartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        credentials.setStatusPayload(null);

        // in case of swedish bankid, we use credentialsId as mfaId
        String mfaId = credentials.getId();
        supplementalRequester.requestSupplementalInformation(mfaId, credentials);

        return mfaId;
    }

    private static Map<String, String> stringToMap(final String string) {
        return SerializationUtils.deserializeFromString(
                string, new TypeReference<HashMap<String, String>>() {});
    }

    private Map<String, String> deserializeSupplementalInformation(String supplementalInformation) {
        return Optional.ofNullable(
                        SerializationUtils.deserializeFromString(
                                supplementalInformation,
                                new TypeReference<HashMap<String, String>>() {}))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "SupplementalInformationResponse cannot be deserialized"));
    }
}
