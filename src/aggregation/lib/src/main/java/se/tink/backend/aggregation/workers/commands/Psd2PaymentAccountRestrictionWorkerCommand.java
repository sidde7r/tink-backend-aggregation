package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
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

    public Psd2PaymentAccountRestrictionWorkerCommand(
            AgentWorkerCommandContext context, CredentialsRequest request) {
        this.context = context;
        this.refreshInformationRequest = request;
        this.regulatoryRestrictions = context.getRegulatoryRestrictions();
        this.psd2PaymentAccountClassifier = context.getPsd2PaymentAccountClassifier();
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
        this.context
                .getAccountDataCache()
                .getFilteredAccountData()
                .forEach(
                        accountData ->
                                // currently we do not want to restrict anything - just see this
                                // command running
                                filterRestrictedAccount(accountData.getAccount()));
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Intentionally left empty.
    }
}
