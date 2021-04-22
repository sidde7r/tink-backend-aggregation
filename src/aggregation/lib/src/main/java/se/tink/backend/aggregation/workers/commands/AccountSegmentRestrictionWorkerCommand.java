package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableSegment;

public class AccountSegmentRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(AccountSegmentRestrictionWorkerCommand.class);

    private final AgentWorkerCommandContext context;
    private final Set<RefreshableSegment> segmentsToRefresh;

    public AccountSegmentRestrictionWorkerCommand(AgentWorkerCommandContext context) {
        Preconditions.checkState(context.getRequest() instanceof RefreshInformationRequest);
        RefreshInformationRequest refreshInformationRequest =
                (RefreshInformationRequest) context.getRequest();
        this.segmentsToRefresh = refreshInformationRequest.getSegmentsToRefresh();
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        // Temporary null/isEmpty check.
        if (CollectionUtils.isEmpty(this.segmentsToRefresh)) {
            log.debug("The segmentsToRefresh was null or empty.");
            return AgentWorkerCommandResult.CONTINUE;
        }

        // Filter any accounts that are not a part of `this.segmentsToRefresh`.
        registerAccountSegmentFilter();

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // NOOP.
    }

    private void registerAccountSegmentFilter() {
        this.context
                .getAccountDataCache()
                .addFilter(
                        this::filterAccountAccordingToSegment,
                        FilterReason.DATA_FETCHING_RESTRICTIONS_SEGMENTS);
    }

    private boolean filterAccountAccordingToSegment(Account account) {
        RefreshableSegment accountSegment =
                Optional.ofNullable(account.getAccountHolder())
                        .map(AccountHolder::getType)
                        .map(this::accountHolderTypeToSegment)
                        .orElse(RefreshableSegment.UNDETERMINED);

        return this.segmentsToRefresh.contains(accountSegment);
    }

    private RefreshableSegment accountHolderTypeToSegment(AccountHolderType accountType) {
        switch (accountType) {
            case BUSINESS:
            case CORPORATE:
                return RefreshableSegment.BUSINESS;
            case PERSONAL:
                return RefreshableSegment.PERSONAL;
            case UNKNOWN:
            default:
                return RefreshableSegment.UNDETERMINED;
        }
    }
}
