package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshScope;

public class AccountSegmentRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(AccountSegmentRestrictionWorkerCommand.class);
    public static final String USE_ACCOUNT_SEGMENT_RESTRICTION_FEATURE_NAME =
            "useAccountSegmentRestriction";

    private final AgentWorkerCommandContext context;
    private final Set<FinancialService.FinancialServiceSegment> segmentsToRefresh;

    public AccountSegmentRestrictionWorkerCommand(AgentWorkerCommandContext context) {
        Preconditions.checkState(context.getRequest() instanceof RefreshInformationRequest);
        RefreshInformationRequest refreshInformationRequest =
                (RefreshInformationRequest) context.getRequest();

        RefreshScope refreshScope = refreshInformationRequest.getRefreshScope();
        this.segmentsToRefresh =
                Objects.nonNull(refreshScope)
                        ? refreshScope.getFinancialServiceSegmentsIn()
                        : Collections.emptySet();
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (!isAccountSegmentRestrictionEnabled()) {
            return AgentWorkerCommandResult.CONTINUE;
        }

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

    private boolean isAccountSegmentRestrictionEnabled() {
        return context.getAgentsServiceConfiguration()
                .isFeatureEnabled(USE_ACCOUNT_SEGMENT_RESTRICTION_FEATURE_NAME);
    }

    private void registerAccountSegmentFilter() {
        this.context
                .getAccountDataCache()
                .addFilter(
                        this::filterAccountAccordingToSegment,
                        FilterReason.DATA_FETCHING_RESTRICTIONS_SEGMENTS);
    }

    private boolean filterAccountAccordingToSegment(Account account) {
        FinancialService.FinancialServiceSegment accountSegment =
                Optional.ofNullable(account.getAccountHolder())
                        .map(AccountHolder::getType)
                        .map(this::accountHolderTypeToSegment)
                        .orElse(FinancialService.FinancialServiceSegment.UNDETERMINED);

        return this.segmentsToRefresh.contains(accountSegment);
    }

    private FinancialService.FinancialServiceSegment accountHolderTypeToSegment(
            AccountHolderType accountType) {
        switch (accountType) {
            case BUSINESS:
            case CORPORATE:
                return FinancialService.FinancialServiceSegment.BUSINESS;
            case PERSONAL:
                return FinancialService.FinancialServiceSegment.PERSONAL;
            case UNKNOWN:
            default:
                return FinancialService.FinancialServiceSegment.UNDETERMINED;
        }
    }
}
