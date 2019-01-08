package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransactionList;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.stream.Stream;

@JsonObject
public class FetchUpcomingTransactionsResponse extends BelfiusResponse {

    public Stream<BelfiusUpcomingTransaction> stream() {
        if (MessageResponse.isError(this)) {
            return Stream.empty();
        }
        return filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> BelfiusConstants.Widget.UPCOMING_TRANSACTIONS.equalsIgnoreCase(widget.getWidgetId()))
                .flatMap(widget -> widget.getProperties(BelfiusUpcomingTransactionList.class).getTransactions().stream());
    }

    public boolean hasNext() {
        if (MessageResponse.isError(this)) {
            return false;
        }
        return ScreenUpdateResponse.findWidget(this, BelfiusConstants.Widget.UPCOMING_TRANSACTIONS_HAS_NEXT)
                .map(widget -> "Y".equalsIgnoreCase(widget.getTextProperty()))
                .orElse(false);
    }
}
