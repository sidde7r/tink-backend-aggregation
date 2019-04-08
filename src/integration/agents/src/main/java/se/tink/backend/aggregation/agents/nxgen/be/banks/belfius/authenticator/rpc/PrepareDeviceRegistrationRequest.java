package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareDeviceRegistrationRequest extends BelfiusRequest {

    public static PrepareDeviceRegistrationRequest.Builder create(
            String deviceToken, String deviceBrand, String deviceName) {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget
                                                .DEVICE_REGISTRATION_PREPARE_REGISTRATION),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION_DEV_TOKEN,
                                        deviceToken),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION_BRAND,
                                        deviceBrand),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION_MODEL,
                                        deviceName)));
    }
}
