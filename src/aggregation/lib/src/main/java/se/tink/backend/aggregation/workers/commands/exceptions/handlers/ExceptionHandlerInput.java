package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionHandlerInput {

    private MetricAction metricAction;
    private Transfer transfer;
    private SignableOperation signableOperation;
    private Catalog catalog;
    private AgentWorkerCommandContext context;
}
