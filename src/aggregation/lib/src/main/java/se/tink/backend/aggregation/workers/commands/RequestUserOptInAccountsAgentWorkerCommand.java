package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.events.IntegrationParameters;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.pair.Pair;

public class RequestUserOptInAccountsAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private static final String IS_CORRECT = "isCorrect";
    private static final String TRUE = "true";

    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final SystemUpdater systemUpdater;
    private final ConfigureWhitelistInformationRequest request;
    private final ControllerWrapper controllerWrapper;
    private final LoginAgentEventProducer loginAgentEventProducer;
    private Long startTime;

    public RequestUserOptInAccountsAgentWorkerCommand(
            AgentWorkerCommandContext context,
            ConfigureWhitelistInformationRequest request,
            ControllerWrapper controllerWrapper,
            LoginAgentEventProducer loginAgentEventProducer) {
        this.context = context;
        this.statusUpdater = context;
        this.systemUpdater = context;
        this.request = request;
        this.controllerWrapper = controllerWrapper;
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.startTime = System.nanoTime();
    }

    // refresh account and send supplemental information to system
    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        // Get accounts that have been fetched from the bank.
        List<Pair<Account, AccountFeatures>> accountsInContext =
                context.getCachedAccountsWithFeatures();
        final List<Account> accountsInRequest = request.getAccounts();

        // if the accounts in context is empty and the account in the request is empty user most
        // likely does not have account under credential, we continue with the rest of the operation
        // without requesting supplemental information
        if (accountsInContext.isEmpty() && accountsInRequest.isEmpty()) {
            log.info(
                    "[optIn] Received no accounts in context and no accounts in request for credentials: {}",
                    Optional.ofNullable(request.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null));
            return AgentWorkerCommandResult.CONTINUE;
        }

        // If the accounts in the context and request are not empty, handle the case for changed
        if (!accountsInContext.isEmpty() && !accountsInRequest.isEmpty()) {
            log.info(
                    "[optIn] Received some accounts in context and some accounts in request for credentials: {}",
                    Optional.ofNullable(request.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null));
            return handleNonEmptyRequestAccountsCase(accountsInContext, accountsInRequest);
        }

        // If the accounts in the context is empty
        // and the accounts in the request is not, notify with supplemental information
        // and await if this is correct or not.
        if (accountsInContext.isEmpty() && !accountsInRequest.isEmpty()) {
            log.info(
                    "[optIn] Received no accounts in context and some accounts in request for credentials: {}",
                    Optional.ofNullable(request.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null));
            return handleEmptyCachedAccountsCase(accountsInRequest);
        }
        log.info(
                "[optIn] Received some accounts in context and no accounts in request for credentials: {}",
                Optional.ofNullable(request.getCredentials()).map(Credentials::getId).orElse(null));
        // If we got here there are not accounts in the request
        // but there are accounts in the context.
        return handleEmptyRequestAccountsCase(accountsInContext);
    }

    @Override
    protected void doPostProcess() {}

    private void filterOptInAccounts(List<String> optInAccountUniqueIds) {
        this.context
                .getAccountDataCache()
                .addFilter(
                        account -> optInAccountUniqueIds.contains(account.getBankId()),
                        FilterReason.OPT_IN);
    }

    private AgentWorkerCommandResult handleEmptyRequestAccountsCase(
            List<Pair<Account, AccountFeatures>> accountsInContext)
            throws SupplementalInfoException {
        Map<String, String> supplementalInformation;
        try {
            supplementalInformation =
                    askAccountSupplementalInformation(accountsInContext, Lists.newArrayList());
        } catch (SupplementalInfoException exception) {
            if (exception.getError() == SupplementalInfoError.NO_VALID_CODE) {
                emitOptInTimeoutEvent();
            }
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        // Abort if the supplemental information is null or empty.
        if (supplementalInformation == null || supplementalInformation.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        updateCredentialsExcludingSensitiveInformation();

        // Add the optIn account id:s to the context to use them when doing the refresh and
        // processing.
        List<String> optInAccounts =
                supplementalInformation.entrySet().stream()
                        .filter(e -> Objects.equals(e.getValue(), "true"))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
        // Abort if there is no optIn accounts matched from supplemental info
        // The main reason is due to unexpected supplemental info back
        if (optInAccounts == null || optInAccounts.isEmpty()) {
            log.error("Empty accounts matched.");
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        filterOptInAccounts(optInAccounts);

        return AgentWorkerCommandResult.CONTINUE;
    }

    private AgentWorkerCommandResult handleEmptyCachedAccountsCase(List<Account> accountsInRequest)
            throws SupplementalInfoException {
        log.info("Got an empty response from the bank, confirming this is correct with the user.");

        Field fields =
                Field.builder()
                        .description(
                                context.getCatalog()
                                        .getString(
                                                "The bank returned an empty list of accounts. Is this correct?"))
                        .masked(false)
                        .checkbox(true)
                        .pattern("true/false")
                        .name(IS_CORRECT)
                        .helpText(
                                context.getCatalog()
                                        .getString(
                                                "Please be aware of that checking this box will delete the history of your accounts"))
                        .build();

        // Send supplemental request to notify the user about empty response from the bank.
        Map<String, String> supplementalInformation = askSupplementalInformation(fields);

        // Abort if the supplemental information is null or empty.
        if (supplementalInformation == null || supplementalInformation.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        updateCredentialsExcludingSensitiveInformation();

        // Check if the empty response is correct or not.
        boolean isCorrect =
                supplementalInformation.entrySet().stream()
                        .filter(entry -> IS_CORRECT.equalsIgnoreCase(entry.getKey()))
                        .anyMatch(entry -> TRUE.equalsIgnoreCase(entry.getValue()));

        // Abort is this is not correct.
        if (!isCorrect) {
            log.warn("The empty response from the bank was not correct. Aborting.");
            statusUpdater.updateStatus(CredentialsStatus.UNCHANGED);
            return AgentWorkerCommandResult.ABORT;
        }

        log.info(
                "The empty response from the bank was correct. Opting out from the accounts in the request "
                        + "(totally {} accounts).",
                accountsInRequest.size());

        // Opt out from all accounts if it's correct.
        optOutAccounts(accountsInRequest.stream().map(Account::getId).collect(Collectors.toList()));

        return AgentWorkerCommandResult.CONTINUE;
    }

    private AgentWorkerCommandResult handleNonEmptyRequestAccountsCase(
            List<Pair<Account, AccountFeatures>> accountsInContext, List<Account> accountsInRequest)
            throws SupplementalInfoException {

        Map<String, String> supplementalInformation;
        try {
            supplementalInformation =
                    askAccountSupplementalInformation(accountsInContext, accountsInRequest);
        } catch (SupplementalInfoException exception) {
            if (exception.getError() == SupplementalInfoError.NO_VALID_CODE) {
                emitOptInTimeoutEvent();
            }
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        // Abort if the supplemental information is null or empty.
        if (supplementalInformation == null || supplementalInformation.isEmpty()) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }

        updateCredentialsExcludingSensitiveInformation();

        // Send supplemental information request to get the optIn account id:s
        List<String> optInAccounts =
                supplementalInformation.entrySet().stream()
                        .filter(e -> Objects.equals(e.getValue(), "true"))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        filterOptInAccounts(optInAccounts);

        // From the optIn accounts get the optOut accounts
        List<String> optOutAccounts =
                accountsInRequest.stream()
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

    private Map<String, String> askAccountSupplementalInformation(
            List<Pair<Account, AccountFeatures>> accountsInContext, List<Account> accountsInRequest)
            throws SupplementalInfoException {
        Field[] accountsSendToUser =
                accountsInContext.stream()
                        .map(
                                accWithFeatures ->
                                        createSupplementalInformationField(
                                                accWithFeatures.first,
                                                accWithFeatures.second,
                                                accountsInRequest))
                        .toArray(Field[]::new);

        return askSupplementalInformation(accountsSendToUser);
    }

    private void updateCredentialsExcludingSensitiveInformation() {
        Credentials credentials = request.getCredentials();
        credentials.setStatus(CredentialsStatus.UPDATING);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);
    }

    private Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        SupplementalInformationController supplementalInformationController =
                new SupplementalInformationControllerImpl(
                        context, request.getCredentials(), null, "opt-in");

        return supplementalInformationController.askSupplementalInformationSync(fields);
    }

    /**
     * Use this method to opt out from accounts.
     *
     * @param optOutAccountIds - the id:s of the accounts to opt out from.
     */
    private void optOutAccounts(List<String> optOutAccountIds) {
        Credentials credentials = request.getCredentials();

        controllerWrapper.optOutAccounts(
                OptOutAccountsRequest.of(
                        credentials.getUserId(), credentials.getId(), optOutAccountIds));
    }

    private Field createSupplementalInformationField(
            Account account, AccountFeatures accountFeatures, List<Account> existingAccounts) {
        boolean isIncluded =
                existingAccounts.stream()
                        .anyMatch(a -> Objects.equals(a.getBankId(), account.getBankId()));

        return Field.builder()
                .description(account.getAccountNumber() + " " + account.getName())
                .masked(false)
                .pattern("true/false")
                .name(account.getBankId())
                .checkbox(true)
                .value(String.valueOf(isIncluded))
                .additionalInfo(createAdditionalInfo(account, accountFeatures))
                .build();
    }

    private String createAdditionalInfo(Account account, AccountFeatures accountFeatures) {
        JsonObject additionalInfo = new JsonObject();

        additionalInfo.addProperty("accountName", account.getName());
        additionalInfo.addProperty("accountNumber", account.getAccountNumber());
        additionalInfo.addProperty("accountType", account.getType().name());
        additionalInfo.addProperty("balance", account.getBalance());
        additionalInfo.addProperty("currencyCode", account.getCurrencyCode());
        additionalInfo.addProperty("holderName", account.getHolderName());
        additionalInfo.addProperty(
                "iban",
                Objects.nonNull(account.getIdentifier(AccountIdentifier.Type.IBAN))
                        ? account.getIdentifier(AccountIdentifier.Type.IBAN).getIdentifier()
                        : null);

        if (accountFeatures == null || account.getType() != AccountTypes.INVESTMENT) {
            return additionalInfo.toString();
        }

        JsonArray portfolioTypes = new JsonArray();

        accountFeatures.getPortfolios().stream()
                .map(p -> p.getType().name())
                .map(JsonPrimitive::new)
                .forEach(portfolioTypes::add);

        additionalInfo.add("portfolioTypes", portfolioTypes);

        return additionalInfo.toString();
    }

    private void emitOptInTimeoutEvent() {
        if (loginAgentEventProducer != null) {
            long finishTime = System.nanoTime();
            long elapsedTime = finishTime - startTime;

            loginAgentEventProducer.sendLoginCompletedEvent(
                    IntegrationParameters.builder()
                            .providerName(context.getRequest().getCredentials().getProviderName())
                            .correlationId(context.getCorrelationId())
                            .appId(context.getAppId())
                            .clusterId(context.getClusterId())
                            .userId(context.getRequest().getCredentials().getUserId())
                            .build(),
                    LoginResult.OPTIN_ERROR_TIMEOUT,
                    elapsedTime,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                            .UserInteractionInformation.MULTIPLE_FACTOR_USER_INTERACTION);
        }
    }
}
