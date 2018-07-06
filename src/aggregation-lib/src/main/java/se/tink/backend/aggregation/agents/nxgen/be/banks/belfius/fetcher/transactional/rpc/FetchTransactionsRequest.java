package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsRequest extends BelfiusRequest {

    public static Builder createInitialRequest(String key) {
        return BelfiusRequest.builder()
                .setRequests(WidgetEventsRequest.create(WidgetEventInformation.newRepeaterValueChangedWidgetEvent(
                        BelfiusConstants.Widget.PRODUCT_LIST_REPEATER_DETAIL, key)),
                        WidgetEventsRequest.create(WidgetEventInformation.newButtonClickedWidgetEvent(
                                BelfiusConstants.Widget.HISTORY_SEARCH)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.HISTORY_FIND),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.HISTORY_TYPE_TRANSACTIONS, "A"),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.HISTORY_CURRENCY, "EUR")));
    }

    public static Builder createNextPageRequest() {
        return BelfiusRequest.builder()
                .setRequests(WidgetEventsRequest.create(WidgetEventInformation.newButtonClickedWidgetEvent(
                                BelfiusConstants.Widget.HISTORY_BTN_NEXT)));
    }
}
