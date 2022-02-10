package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.sebkort;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebKortSanitizeUniqueIdentifierMgration extends AgentVersionMigration {

    private static final String OLD_SEBKORT_AGENT = "creditcards.sebkort.SEBKortAgent";

    private static final Pattern MASKED_CARD_NUMBER_PATTERN =
            Pattern.compile("^[0-9]{6}\\*{6}[0-9]{4}$");
    private static final Pattern SANITIZED_PATTERN = Pattern.compile("^[0-9]{10}$");

    private static final Map<String, String> AGENT_CLASS_MAPPING =
            new ImmutableMap.Builder<String, String>()
                    .put(
                            "chse:0086",
                            "nxgen.se.creditcards.sebkort.chevrolet.ChevroletMastercardAgent")
                    .put(
                            "cose:0108",
                            "nxgen.se.creditcards.sebkort.nordicchoice.NordicChoiceClubMastercardSEAgent")
                    .put(
                            "djse:0116",
                            "nxgen.se.creditcards.sebkort.djurgardskortet.DjurgardskortetMastercardAgent")
                    .put("ecse:0005", "nxgen.se.creditcards.sebkort.eurocard.EurocardSEAgent")
                    .put(
                            "fase:0129",
                            "nxgen.se.creditcards.sebkort.finnair.FinnairMastercardSEAgent")
                    .put("jese:0032", "nxgen.se.creditcards.sebkort.ingo.IngoMastercardAgent")
                    .put(
                            "nkse:0007",
                            "nxgen.se.creditcards.sebkort.nknyckeln.NkNyckelnMastercardAgent")
                    .put("opse:0107", "nxgen.se.creditcards.sebkort.opel.OpelMastercardAgent")
                    .put(
                            "sase:0102",
                            "nxgen.se.creditcards.sebkort.saseurobonus.SasEurobonusMastercardSEAgent")
                    .put("sbse:0106", "nxgen.se.creditcards.sebkort.saab.SaabMastercardAgent")
                    .put("sjse:0104", "nxgen.se.creditcards.sebkort.sjprio.SjPrioMastercardAgent")
                    .put(
                            "stse:0122",
                            "nxgen.se.creditcards.sebkort.circlek.CircleKMastercardSEAgent")
                    .put(
                            "wase:0121",
                            "nxgen.se.creditcards.sebkort.sebwallet.SebWalletMastercardAgent")
                    .build();

    private static final List<String> NEW_SEBKORT_AGENT_CLASSES =
            ImmutableList.copyOf(AGENT_CLASS_MAPPING.values());

    @Override
    public boolean shouldChangeRequest(CredentialsRequest request) {
        String agentName = request.getProvider().getClassName();
        return (agentName.endsWith(OLD_SEBKORT_AGENT)
                || NEW_SEBKORT_AGENT_CLASSES.stream()
                        .anyMatch(newAgent -> agentName.endsWith(newAgent)));
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
        request.getAccounts().stream()
                .filter(a -> MASKED_CARD_NUMBER_PATTERN.matcher(a.getBankId()).matches())
                .forEach(a -> a.setBankId(sanitizeUniqueIdentifier(a.getBankId())));
    }

    private String sanitizeUniqueIdentifier(String uniqueIdentifier) {
        return uniqueIdentifier.replaceAll("[^\\dA-Za-z]", "");
    }
}
