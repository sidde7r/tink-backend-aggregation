package se.tink.backend.aggregation.workers.commands;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;

/**
 * TODO adding metrics if necessary
 **/
public class RequestUserOptInAccountsAgentWorkerCommand extends AgentWorkerCommand{
    private static final Logger log = LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private final AgentWorkerContext context;
    private final RefreshWhitelistInformationRequest request;
    private final SupplementalInformationController supplementalInformationController;

    public RequestUserOptInAccountsAgentWorkerCommand(AgentWorkerContext context, RefreshWhitelistInformationRequest request) {
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
            context.getRequest().getCredentials().setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        context.addOptInAccountUniqueId(supplementalResponse.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), "true"))
                .map(Map.Entry::getKey).collect(Collectors.toList()));

        return AgentWorkerCommandResult.CONTINUE;
    }

    private Field createSupplementalInformationField(Account account, List<Account>
            existingAccounts){
        boolean isIncluded = existingAccounts.stream()
                .anyMatch(a -> Objects.equals(a.getBankId(), account.getBankId()));
        Field field = new Field();
        field.setDescription(account.getAccountNumber() + " " + account.getName());
        field.setMasked(false);
        field.setPattern("(true|false)");
        field.setName(account.getBankId());
        field.setCheckbox(true);
        field.setValue(String.valueOf(isIncluded));
        return field;
    }

    @Override
    public void postProcess() {
        // clear out supplemental information to not contain any sensitive information
        Credentials credentials = context.getRequest().getCredentials();
        credentials.setSupplementalInformation(null);
        context.updateCredentialsExcludingSensitiveInformation(credentials);
    }

}
