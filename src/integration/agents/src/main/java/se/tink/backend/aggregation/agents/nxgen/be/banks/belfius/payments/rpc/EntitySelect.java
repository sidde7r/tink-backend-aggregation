package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;

public class EntitySelect extends BelfiusRequest {
    public static BelfiusRequest.Builder create(String sessionId) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newRepeaterValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.ENTITY_SWITCHER, "1")))
                .setSessionId(sessionId);
    }

    public static BelfiusRequest.Builder createWithCardNumber(String sessionId, String cardNumber) {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        "Container@minp_CardNr", cardNumber),
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        "Container@b_LoginPW")))
                .setSessionId(sessionId);
    }
}
