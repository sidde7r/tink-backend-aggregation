package se.tink.backend.aggregation.workers.commands;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;

/**
 * TODO adding metrics if necessary
 **/
public class RequestUserOptInAccountsAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private static final String IS_CORRECT = "isCorrect";
    private static final String TRUE = "true";

    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final ConfigureWhitelistInformationRequest request;
    private final Credentials credentials;
    private final SupplementalInformationController supplementalInformationController;
    private final List<Account> accountsInRequest;
    private List<Account> accountsInContext;
    private final Catalog catalog;
    private final ControllerWrapper controllerWrapper;
    private final SystemUpdater systemUpdater;

    public RequestUserOptInAccountsAgentWorkerCommand(AgentWorkerCommandContext context,
            ConfigureWhitelistInformationRequest request,
            ControllerWrapper controllerWrapper) {
        this.context = context;
        this.statusUpdater = context;
        this.systemUpdater = context;
        this.request = request;
        this.credentials = request.getCredentials();
        this.supplementalInformationController = new SupplementalInformationController(context , request.getCredentials());
        this.accountsInRequest = request.getAccounts();
        this.catalog = context.getCatalog();
        this.controllerWrapper = controllerWrapper;
    }

    // refresh account and send supplemental information to system
    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        // Get accounts that have been fetched from the bank.
        this.accountsInContext = context.getCachedAccounts();

        // If the accounts in the context is empty and the accounts in the request is not notify with supplemental
        // information and await if this is correct or not.
        if (accountsInContext.isEmpty() && !accountsInRequest.isEmpty()) {
            return handleEmptyCachedAccountsCase();
        }

        // If the accounts in the context isn't empty and the accounts in the request isn't empty handle the case
        // for changed
        if (!accountsInContext.isEmpty() && !accountsInRequest.isEmpty()) {
            return handleNonEmptyRequestAccountsCase();
        }

        // if the accounts in context is emptu and the account in the request is empty user most likely does not have
        // account under credential, we continue with the rest of the operation without requesting supplemental
        // information
        if (accountsInContext.isEmpty() && accountsInRequest.isEmpty()) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        // If we got here there are not accounts in the request but there are accounts in the context.
        Field[] accountsSendToUser = accountsInContext.stream()
                .map(account -> createSupplementalInformationField(account, accountsInRequest))
                .toArray(Field[]::new);

        // Send supplemental information to the user to ask what accounts to optIn for.
        Map<String, String> supplementalResponse = supplementalInformationController.askSupplementalInformation
                (accountsSendToUser);

        // Abort if the supplemental information is null or empty.
        if (supplementalResponse == null || supplementalResponse.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        credentials.setStatus(CredentialsStatus.UPDATING);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

        // Add the optIn account id:s to the context to use them when doing the refresh and processing.
        context.addOptInAccountUniqueId(supplementalResponse.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), "true"))
                .map(Map.Entry::getKey).collect(Collectors.toList()));

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
    }

    private AgentWorkerCommandResult handleEmptyCachedAccountsCase() throws SupplementalInfoException {
        log.info("Got an empty response from the bank, confirming this is correct with the user.");

        // Send supplemental request to notify the user about empty response from the bank.
        Map<String, String> supplementalInformation = supplementalInformationController.askSupplementalInformation(
                Field.builder()
                        .description(catalog.getString("The bank returned an empty list of accounts. Is this correct?"))
                        .masked(false)
                        .checkbox(true)
                        .pattern("true/false")
                        .name("isCorrect")
                        .helpText(catalog.getString("Please be aware of that checking this box will delete the history of your accounts"))
                        .build());

        // Abort if the supplemental information is null or empty.
        if (supplementalInformation == null || supplementalInformation.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        credentials.setStatus(CredentialsStatus.UPDATING);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

        // Check if the empty response is correct or not.
        boolean isCorrect = supplementalInformation.entrySet().stream()
                .filter(entry -> IS_CORRECT.equalsIgnoreCase(entry.getKey()))
                .anyMatch(entry -> TRUE.equalsIgnoreCase(entry.getValue()));

        // Abort is this is not correct.
        if (!isCorrect) {
            log.warn("The empty response from the bank was not correct. Aborting.");
            return AgentWorkerCommandResult.ABORT;
        }

        log.info("The empty response from the bank was correct. Opting out from the accounts in the request "
                + "(totally {} accounts).", accountsInRequest.size());

        // Opt out from all accounts if it's correct.
        optOutAccounts(accountsInRequest.stream()
                .map(Account::getId)
                .collect(Collectors.toList()));

        return AgentWorkerCommandResult.CONTINUE;
    }

    private AgentWorkerCommandResult handleNonEmptyRequestAccountsCase() throws SupplementalInfoException {
        Field[] accountsSendToUser = accountsInContext.stream()
                .map(account -> createSupplementalInformationField(account, accountsInRequest))
                .toArray(Field[]::new);

        Map<String, String> supplementalInformation = supplementalInformationController.askSupplementalInformation(
                accountsSendToUser);

        // Abort if the supplemental information is null or empty.
        if (supplementalInformation == null || supplementalInformation.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        credentials.setStatus(CredentialsStatus.UPDATING);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

        // Send supplemental information request to get the optIn account id:s
        List<String> optInAccounts = supplementalInformation.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), "true"))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        // Add the optIn account id:s to the context to use them when doing the refresh and processing.
        context.addOptInAccountUniqueId(optInAccounts);

        // From the optIn accounts get the optOut accounts
        List<String> optOutAccounts = accountsInRequest.stream()
                .filter(account -> !optInAccounts.contains(account.getBankId()))
                .map(Account::getId)
                .collect(Collectors.toList());

        // If there are no optOut accounts we can continue without doing anything more.
        if (optOutAccounts.isEmpty()) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        // Opt out from the optOut accounts.
        optOutAccounts(optOutAccounts);

        return AgentWorkerCommandResult.CONTINUE;
    }

    /**
     *
     * Use this method to opt out from accounts.
     *
     * @param optOutAccountIds - the id:s of the accounts to opt out from.
     */
    private void optOutAccounts(List<String> optOutAccountIds) {
        Credentials credentials = request.getCredentials();

        controllerWrapper.optOutAccounts(
                OptOutAccountsRequest.of(credentials.getUserId(), credentials.getId(), optOutAccountIds));
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

    private String createAdditionalInfo(Account account) {
        JsonObject additionalInfo = new JsonObject();

        additionalInfo.addProperty("accountName", account.getName());
        additionalInfo.addProperty("accountNumber", account.getAccountNumber());
        additionalInfo.addProperty("accountType", account.getType().name());
        additionalInfo.addProperty("balance", account.getBalance());
        additionalInfo.addProperty("holderName", account.getHolderName());
        additionalInfo.addProperty("iban", Objects.nonNull(account.getIdentifier(AccountIdentifier.Type.IBAN)) ?
                account.getIdentifier(AccountIdentifier.Type.IBAN).getIdentifier() : null);

        return additionalInfo.toString();
    }
}
