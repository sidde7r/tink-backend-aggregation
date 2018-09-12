package se.tink.backend.aggregation.workers.commands;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.libraries.account.AccountIdentifier;

/**
 * TODO adding metrics if necessary
 **/
public class RequestUserOptInAccountsAgentWorkerCommand extends AgentWorkerCommand{
    private static final Logger log = LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private final AgentWorkerContext context;
    private final ConfigureWhitelistInformationRequest request;
    private final SupplementalInformationController supplementalInformationController;

    public RequestUserOptInAccountsAgentWorkerCommand(AgentWorkerContext context,
            ConfigureWhitelistInformationRequest request) {
        this.context = context;
        this.request = request;
        this.supplementalInformationController = new SupplementalInformationController(context , request.getCredentials());
    }

    // refresh account and send supplemental information to system
    @Override
    public AgentWorkerCommandResult execute() throws Exception {

        // accounts are put in context by refresh information command
        List<Account> accountsInContext = context.getCachedAccounts();
        // create fields for supplemental information
        // each field contains the account number as name, and a boolean as value indicating if the account is white
        // listed currently
        Field[] accountsSendToUser = accountsInContext.stream()
                .map(account -> createSupplementalInformationField(account, request.getAccounts()))
                .toArray(Field[]::new);


        Map<String, String> supplementalResponse = supplementalInformationController.askSupplementalInformation
                (accountsSendToUser);

        // check if user send back the accounts
        if (supplementalResponse == null || supplementalResponse.isEmpty()) {
            context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        context.addOptInAccountUniqueId(supplementalResponse.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), "true"))
                .map(Map.Entry::getKey).collect(Collectors.toList()));

        return AgentWorkerCommandResult.CONTINUE;
    }

    private Field createSupplementalInformationField(Account account, List<Account> existingAccounts) {
        boolean isIncluded = existingAccounts.stream()
                .anyMatch(a -> Objects.equals(a.getBankId(), account.getBankId()));

        return Field.builder()
                .description(account.getAccountNumber() + " " + account.getName())
                .masked(false)
                .pattern("true/false")
                .name(account.getBankId())
                .checkbox(true)
                .value(String.valueOf(isIncluded))
                .additionalInfo(createAdditionalInfo(account))
                .build();
    }

    @Override
    public void postProcess() {
    }

    private String createAdditionalInfo(Account account) {
        JsonObject additionalInfo = new JsonObject();

        additionalInfo.addProperty("accountName", account.getName());
        additionalInfo.addProperty("accountType", account.getType().name());
        additionalInfo.addProperty("balance", account.getBalance());
        additionalInfo.addProperty("holderName", account.getHolderName());
        additionalInfo.addProperty("iban", Objects.nonNull(account.getIdentifier(AccountIdentifier.Type.IBAN)) ?
                account.getIdentifier(AccountIdentifier.Type.IBAN).getIdentifier() : null);

        return additionalInfo.toString();
    }
}
