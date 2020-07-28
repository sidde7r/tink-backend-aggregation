package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class Psd2PaymentAccountRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(Psd2PaymentAccountRestrictionWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final CredentialsRequest refreshInformationRequest;
    private final RegulatoryRestrictions regulatoryRestrictions;
    private final Psd2PaymentAccountClassifier psd2PaymentAccountClassifier;
    private final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    public Psd2PaymentAccountRestrictionWorkerCommand(
            AgentWorkerCommandContext context,
            CredentialsRequest request,
            RegulatoryRestrictions regulatoryRestrictions,
            Psd2PaymentAccountClassifier psd2PaymentAccountClassifier,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {
        this.context = context;
        this.refreshInformationRequest = request;
        this.regulatoryRestrictions = regulatoryRestrictions;
        this.psd2PaymentAccountClassifier = psd2PaymentAccountClassifier;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;
    }

    private boolean filterRestrictedAccount(Account account) {
        Optional<Psd2PaymentAccountClassificationResult> classification =
                psd2PaymentAccountClassifier.classify(
                        refreshInformationRequest.getProvider(), account);
        boolean shouldRestrict =
                regulatoryRestrictions.shouldAccountBeRestricted(
                        refreshInformationRequest, account, classification);
        log.info(
                "Restricting account (credentials, account type, classification): ({}, {}, {})",
                refreshInformationRequest.getCredentials().getId(),
                account.getType(),
                classification.orElse(null));
        return shouldRestrict;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        try {
            this.context
                    .getAccountDataCache()
                    .getFilteredAccountData()
                    .forEach(
                            accountData -> {
                                // currently we do not want to restrict anything - just see this
                                // command running
                                Account account = accountData.getAccount();
                                filterRestrictedAccount(account);
                                sendEvents(account);
                            });
        } catch (RuntimeException e) {
            log.warn("Could not execute Psd2PaymentAccountRestrictionWorkerCommand", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private void sendEvents(Account account) {
        sendPsd2PaymentAccountClassificationEvent(account);
        sendSourceInfoEvent(account);
    }

    private void sendPsd2PaymentAccountClassificationEvent(Account account) {
        CredentialsRequest request = context.getRequest();
        if (request.getProvider() == null) {
            return;
        }
        Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification =
                psd2PaymentAccountClassifier.classify(request.getProvider(), account);

        paymentAccountClassification.ifPresent(
                classification ->
                        accountInformationServiceEventsProducer
                                .sendPsd2PaymentAccountClassificationEvent(
                                        context.getClusterId(),
                                        context.getAppId(),
                                        request.getUser().getId(),
                                        request.getProvider(),
                                        context.getCorrelationId(),
                                        request.getCredentials().getId(),
                                        account.getId(),
                                        account.getType().name(),
                                        classification.name(),
                                        account.getCapabilities()));
    }

    private void sendSourceInfoEvent(Account account) {
        CredentialsRequest request = context.getRequest();
        if (request.getProvider() == null) {
            return;
        }
        accountInformationServiceEventsProducer.sendAccountSourceInfoEvent(
                context.getClusterId(),
                context.getAppId(),
                request.getUser().getId(),
                request.getProvider(),
                context.getCorrelationId(),
                request.getCredentials().getId(),
                account.getId(),
                account.getSourceInfo());
    }

    @Override
    public void postProcess() throws Exception {
        // Intentionally left empty.
    }
}
