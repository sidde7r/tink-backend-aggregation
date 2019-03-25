package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.sebkort;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SebKortSanitizeUniqueIdentifierMgration extends AgentVersionMigration {

    private static final String OLD_SEBKORT_AGENT = "creditcards.sebkort.SEBKortAgent";

    private static final Pattern MASKED_CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{6}\\*{6}[0-9]{4}$");
    private static final Pattern SANITIZED_PATTERN = Pattern.compile("^[0-9]{10}$");

    private static final Map<String, String> AGENT_CLASS_MAPPING = ImmutableMap.of(
            "sjse:0104", "nxgen.se.creditcards.sebkort.sjprio.SjPrioMastercardAgent",
            "sase:0102", "nxgen.se.creditcards.sebkort.saseurobonus.SasEurobonusMastercardSEAgent",
            "ecse:0005", "nxgen.se.creditcards.sebkort.eurocard.EurocardSEAgent"
    );

    private static final List<String> NEW_SEBKORT_AGENT_CLASSES = ImmutableList.copyOf(
            AGENT_CLASS_MAPPING.values()
    );

    @Override
    public boolean shouldChangeRequest(CredentialsRequest request) {
        String agentName = request.getProvider().getClassName();
        if (agentName.endsWith(OLD_SEBKORT_AGENT) ||
                NEW_SEBKORT_AGENT_CLASSES.stream().anyMatch(newAgent -> agentName.endsWith(newAgent))) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldMigrateData(CredentialsRequest request) {
        String agentName = request.getProvider().getClassName();
        if (NEW_SEBKORT_AGENT_CLASSES.stream().anyMatch(newAgent -> agentName.endsWith(newAgent))) {
            return request.getAccounts().stream()
                    .filter(a -> AccountTypes.CREDIT_CARD == a.getType())
                    .anyMatch(a -> MASKED_CARD_NUMBER_PATTERN.matcher(a.getBankId()).matches());
        } else {
            return false;
        }
    }

    private boolean isDataAlreadyMigrated(CredentialsRequest request) {
        return request.getAccounts().stream()
                .filter(a -> AccountTypes.CREDIT_CARD == a.getType())
                .anyMatch(a -> SANITIZED_PATTERN.matcher(a.getBankId()).matches());
    }

    @Override
    public void changeRequest(CredentialsRequest request) {
        if (isDataAlreadyMigrated(request)) {
            String payload = request.getProvider().getPayload();
            request.getProvider().setClassName(AGENT_CLASS_MAPPING.get(payload));
        }
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request
                .getAccounts()
                .stream()
                .filter(a -> MASKED_CARD_NUMBER_PATTERN.matcher(a.getBankId()).matches())
                .forEach(
                        a->
                                a.setBankId(sanitizeUniqueIdentifier(a.getBankId()))
                );
    }

    private String sanitizeUniqueIdentifier(String uniqueIdentifier) {
        return uniqueIdentifier.replaceAll("[^\\dA-Za-z]", "");
    }
}
