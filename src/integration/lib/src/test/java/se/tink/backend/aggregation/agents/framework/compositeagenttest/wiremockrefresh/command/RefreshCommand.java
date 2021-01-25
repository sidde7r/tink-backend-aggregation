package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.command;

import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public final class RefreshCommand implements CompositeAgentTestCommand {

    private final Agent agent;
    private final Credentials credential;
    private final NewAgentTestContext context;
    private final Set<RefreshableItem> refreshableItems;

    @Inject
    private RefreshCommand(
            Agent agent,
            Credentials credential,
            NewAgentTestContext context,
            RefreshInformationRequest refreshInformationRequest) {
        this.agent = agent;
        this.credential = credential;
        this.context = context;
        this.refreshableItems = refreshInformationRequest.getItemsToRefresh();
    }

    @Override
    public void execute() throws Exception {
        credential.setStatus(CredentialsStatus.UPDATING);

        if (agent instanceof DeprecatedRefreshExecutor) {

            ((DeprecatedRefreshExecutor) agent).refresh();
        } else {
            List<RefreshableItem> sortedItems = RefreshableItem.sort(refreshableItems);
            for (RefreshableItem item : sortedItems) {
                if (item == RefreshableItem.IDENTITY_DATA
                        && agent instanceof RefreshIdentityDataExecutor) {

                    context.cacheIdentityData(
                            ((RefreshIdentityDataExecutor) agent)
                                    .fetchIdentityData()
                                    .getIdentityData());
                } else {
                    RefreshExecutorUtils.executeSegregatedRefresher(agent, item, context);
                }
            }
        }

        context.printCollectedData();
    }
}
