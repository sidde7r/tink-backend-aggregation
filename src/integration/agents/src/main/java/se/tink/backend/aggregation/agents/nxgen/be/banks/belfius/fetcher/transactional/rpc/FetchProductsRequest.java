package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchProductsRequest extends BelfiusRequest {

    public static Builder create() {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.PRODUCT_LIST_LOAD)));
    }
}
