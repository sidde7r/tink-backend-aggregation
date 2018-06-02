package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransactionList;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse extends BelfiusResponse {

    public Stream<BelfiusTransaction> stream() {
        return filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> BelfiusConstants.Widget.HISTORY_HIST.equalsIgnoreCase(widget.getWidgetId()))
                .flatMap(widget -> widget.getProperties(BelfiusTransactionList.class).getTransactions().stream());
    }
}
