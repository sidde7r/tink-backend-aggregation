package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.framework.testserverclient.AgentTestServerClient;
import se.tink.backend.aggregation.agents.framework.utils.CliPrintUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class AgentTestServerSupplementalRequester implements SupplementalRequester {
    private static final Logger log =
            LoggerFactory.getLogger(AgentTestServerSupplementalRequester.class);

    private final Credentials credential;
    private final AgentTestServerClient agentTestServerClient;

    @Inject
    public AgentTestServerSupplementalRequester(
            Credentials credential, AgentTestServerClient agentTestServerClient) {
        this.credential = credential;
        this.agentTestServerClient = agentTestServerClient;
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        if (Strings.isNullOrEmpty(autoStartToken)) {
            log.info(String.format("[CredentialsId:%s]: Open BankID", credential.getId()));
        } else {
            log.info(
                    String.format(
                            "[CredentialsId:%s]: Sending autoStartToken to test server: %s",
                            credential.getId(), autoStartToken));
            agentTestServerClient.sendBankIdAutoStartToken(autoStartToken);
        }
    }

    @Override
    public String requestSupplementalInformation(
            Credentials credentials, long waitFor, TimeUnit timeUnit, boolean wait) {
        log.info(
                "Requesting additional info from client. Status: {}, wait: {}",
                credentials.getStatus(),
                wait);

        switch (credentials.getStatus()) {
            case AWAITING_SUPPLEMENTAL_INFORMATION:
                displaySupplementalInformation(credentials);

                agentTestServerClient.initiateSupplementalInformation(
                        credentials.getId(), credentials.getSupplementalInformation());

                if (!wait) {
                    // The agent is not interested in the result. This is the same logic as the
                    // production code.
                    return null;
                }

                Optional<String> supplementalInformation =
                        waitForSupplementalInformation(credentials.getId(), 2, TimeUnit.MINUTES);

                return supplementalInformation.orElse(null);
            case AWAITING_MOBILE_BANKID_AUTHENTICATION:
                // Do nothing as we cannot communicate to the app to open BankId.
                return null;
            case AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
                agentTestServerClient.openThirdPartyApp(credentials.getSupplementalInformation());
                return null;
            default:
                Assert.fail(
                        String.format(
                                "Cannot handle credentials status: %s", credentials.getStatus()));
                return null;
        }
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {
        return Optional.ofNullable(
                agentTestServerClient.waitForSupplementalInformation(mfaId, waitFor, unit));
    }

    private void displaySupplementalInformation(Credentials credentials) {
        log.info("Requesting supplemental information.");

        List<Field> supplementalInformation =
                SerializationUtils.deserializeFromString(
                        credentials.getSupplementalInformation(),
                        new TypeReference<List<Field>>() {});

        List<Map<String, String>> output =
                supplementalInformation.stream()
                        .map(
                                field -> {
                                    Map<String, String> row = new LinkedHashMap<>();
                                    row.put("name", field.getName());
                                    row.put("description", field.getDescription());
                                    row.put("helpText", field.getHelpText());
                                    row.put("masked", String.valueOf(field.isMasked()));
                                    row.put("sensitive", String.valueOf(field.isSensitive()));
                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(0, "supplemental information", output);
    }
}
