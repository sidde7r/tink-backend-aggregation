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
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SupplementalInformationControllerImpl implements SupplementalInformationController {
    private static final Logger logger =
            LoggerFactory.getLogger(SupplementalInformationControllerImpl.class);

    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private static final int TIMEOUT_MINUTES_EMBEDDED_FIELDS = 2;
    private static final int TIMEOUT_MINUTES_MOBILE_BANKID = 2;

    private final SupplementalRequester supplementalRequester;
    private final Credentials credentials;
    private final String state;

    /**
     * Do not construct your own SupplementalInfomationController. Use the instance available to
     * your agent from SubsequentGenerationAgent instead. Or even better, migrate to
     * AgentPlatformAgent or SubsequentProgressiveGenerationAgent where the Supplemental information
     * controlling is outside of the agent and you do not need to have an instance.
     */
    public SupplementalInformationControllerImpl(
            SupplementalRequester supplementalRequester, Credentials credentials, String state) {
        this.supplementalRequester = supplementalRequester;
        this.credentials = credentials;
        this.state = state;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, boolean allowEmptyString) {

        Optional<String> result =
                supplementalRequester.waitForSupplementalInformation(mfaId, waitFor, unit);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        // REMOVE THIS ONES NEMID/TINKLINK/NORDEA IS FIXED TO NOT SEND EMPTY STRING
        if (allowEmptyString && "".equals(result.get())) {
            return Optional.of(new HashMap<>());
        }

        if (Strings.isNullOrEmpty(result.get())) {
            return Optional.empty();
        }

        return Optional.ofNullable(stringToMap(result.get()));
    }

    @Override
    public Map<String, String> askSupplementalInformationSync(Field... fields)
            throws SupplementalInfoException {

        String mfaId = askSupplementalInformationAsync(fields);

        Optional<String> results =
                supplementalRequester.waitForSupplementalInformation(
                        mfaId, TIMEOUT_MINUTES_EMBEDDED_FIELDS, TimeUnit.MINUTES);

        String supplementalInformation =
                Optional.ofNullable(Strings.emptyToNull(results.orElse(null)))
                        .orElseThrow(SupplementalInfoError.NO_VALID_CODE::exception);

        Map<String, String> suplementalInformation =
                deserializeSupplementalInformation(supplementalInformation);
        logger.info("Finished requesting supplemental information");
        suplementalInformation.forEach(
                (key, value) -> {
                    String message = value == null ? "equals null" : "has length " + value.length();
                    logger.info("supplemental information {} {}", key, message);
                });
        return suplementalInformation;
    }

    @Override
    public String askSupplementalInformationAsync(Field... fields) {
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        String names = Arrays.stream(fields).map(Field::getName).collect(Collectors.joining(","));
        logger.info("Requesting for fields: {}", names);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        // in case of embedded supplemental information, we use credentialsId as mfaId
        return credentials.getId();
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

        final String deepLinkUrl =
                Optional.ofNullable(payload.getIos()).map(Ios::getDeepLinkUrl).orElse("<none>");

        logger.info("Opening third party app with deep link URL {}, state {}", deepLinkUrl, state);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        // return the mfaId that can be listened for.
        return String.format(UNIQUE_PREFIX_TPCB, this.state);
    }

    @Override
    public void openMobileBankIdSync(String autoStartToken) {
        String mfaId = openMobileBankIdAsync(autoStartToken);

        supplementalRequester.waitForSupplementalInformation(
                mfaId, TIMEOUT_MINUTES_MOBILE_BANKID, TimeUnit.MINUTES);
    }

    @Override
    public String openMobileBankIdAsync(String autoStartToken) {
        credentials.setSupplementalInformation(autoStartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        // in case of swedish bankid, we use credentialsId as mfaId
        return credentials.getId();
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
