package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchUpcomingTransactionsRequest extends BelfiusRequest {

    public static Builder createInitialRequest(String key) {
        return BelfiusRequest.builder()
                .setRequests(WidgetEventsRequest.create(WidgetEventInformation.newRepeaterValueChangedWidgetEvent(
                        BelfiusConstants.Widget.PRODUCT_LIST_REPEATER_DETAIL, key)),
                        WidgetEventsRequest.create(WidgetEventInformation.newButtonClickedWidgetEvent(
                                BelfiusConstants.Widget.UPCOMING_TRANSACTIONS_BUTTON_PENDING)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.UPCOMING_TRANSACTIONS_BUTTON_FIND)));
    }

    public static Builder createNextPageRequest() {
        return BelfiusRequest.builder()
                .setRequests(WidgetEventsRequest.create(WidgetEventInformation.newButtonClickedWidgetEvent(
                        BelfiusConstants.Widget.UPCOMING_TRANSACTIONS_BTN_NEXT)));
    }
}
