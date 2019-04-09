package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransactionList;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse extends BelfiusResponse {

    public Stream<BelfiusTransaction> stream() {

        // xiacheng: when we fetch too old data, we receive
        //        {
        //            "pendingResponseSets": false,
        //                "responseSets": [
        //            {
        //                "requestCounter": -1,
        //                    "applicationId": "Unknown",
        //                    "responses": [
        //                {
        //                    "MessageResponse": [
        //                    {
        //                        "messageContent": "Technische error / Erreur technique / Technical
        // error : ticket = PRS/s6/1544775139017 [SECURITY ISSUE]"
        //                    },
        //                    {
        //                        "messageDetail": ""
        //                    },
        //                    {
        //                        "messageType": "fatal"
        //                    },
        //                    {
        //                        "messageTarget": "internal_and_container"
        //                    },
        //                    {
        //                        "messageCode": "SECURITY ISSUE"
        //                    }
        //                    ]
        //                }
        //                ]
        //            }
        //            ]
        //        }
        if (MessageResponse.isError(this)) {
            return Stream.empty();
        }

        return filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(
                        widget ->
                                BelfiusConstants.Widget.HISTORY_HIST.equalsIgnoreCase(
                                        widget.getWidgetId()))
                .flatMap(
                        widget ->
                                widget.getProperties(BelfiusTransactionList.class).getTransactions()
                                        .stream());
    }

    public boolean hasNext() {
        if (MessageResponse.isError(this)) {
            return false;
        }
        return ScreenUpdateResponse.findWidget(this, BelfiusConstants.Widget.HISTORY_HAS_NEXT)
                .map(widget -> "Y".equalsIgnoreCase(widget.getTextProperty()))
                .orElse(false);
    }
}
