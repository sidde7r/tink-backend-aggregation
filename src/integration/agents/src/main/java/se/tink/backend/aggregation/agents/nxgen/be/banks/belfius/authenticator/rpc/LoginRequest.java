package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest extends BelfiusRequest {

    public static Builder create(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                newInputEvent(
                                        BelfiusConstants.Widget.VERSION_KIND_APP,
                                        BelfiusConstants.Request.VERSION_KIND_APP),
                                newInputEvent(
                                        BelfiusConstants.Widget.DEVICE_TOKEN_HASHED,
                                        deviceTokenHashed),
                                newInputEvent(
                                        BelfiusConstants.Widget.DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                                        deviceTokenHashedIosComparison),
                                newInputEvent(BelfiusConstants.Widget.SIGNATURE, signature),
                                newInputEvent(BelfiusConstants.Widget.IS_GUEST, "N"),
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.AUTHENTICATE),
                                newInputEvent(BelfiusConstants.Widget.TYPE_LOGON_DEVICE, ""),
                                newInputEvent(BelfiusConstants.Widget.CODE_SDK, "I"),
                                newInputEvent(BelfiusConstants.Widget.DEV_T, "I"),
                                newInputEvent(BelfiusConstants.Widget.ROOT, "N"),
                                newInputEvent(BelfiusConstants.Widget.ROOT_H, "N"),
                                newInputEvent(BelfiusConstants.Widget.EMUL, "N"),
                                newInputEvent(BelfiusConstants.Widget.DEBUG, "N"),
                                newInputEvent(BelfiusConstants.Widget.TAMPER, "N"),
                                newInputEvent(BelfiusConstants.Widget.NS_WIFI, "N"),
                                newInputEvent(BelfiusConstants.Widget.UNKW_S, "N"),
                                newInputEvent(BelfiusConstants.Widget.HOST, "N"),
                                newInputEvent(BelfiusConstants.Widget.MALW, "N"),
                                newInputEvent(BelfiusConstants.Widget.MALW_L, ""),
                                newInputEvent(BelfiusConstants.Widget.SMS_LIS, "N"),
                                newInputEvent(BelfiusConstants.Widget.FACE_D, "N"),
                                newInputEvent(
                                        BelfiusConstants.Widget.SYS_VER,
                                        BelfiusConstants.Request.SYS_VER),
                                newInputEvent(
                                        BelfiusConstants.Widget.APP_VER,
                                        BelfiusConstants.Request.APP_VER)));
    }

    public static Builder createPw(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                newInputEventPw(
                                        BelfiusConstants.Widget.VERSION_KIND_APP,
                                        BelfiusConstants.Request.VERSION_KIND_APP),
                                newInputEventPw(
                                        BelfiusConstants.Widget.DEVICE_TOKEN_HASHED,
                                        deviceTokenHashed),
                                newInputEventPw(
                                        BelfiusConstants.Widget.DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                                        deviceTokenHashedIosComparison),
                                newInputEventPw(BelfiusConstants.Widget.SIGNATURE, signature),
                                newInputEventPw(BelfiusConstants.Widget.IS_GUEST, "N"),
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        "Container@reuse_LoginPw@btn_authenticate"),
                                newInputEventPw(BelfiusConstants.Widget.TYPE_LOGON_DEVICE, ""),
                                newInputEventPw(BelfiusConstants.Widget.CODE_SDK, "I"),
                                newInputEventPw(BelfiusConstants.Widget.DEV_T, "I"),
                                newInputEventPw(BelfiusConstants.Widget.ROOT, "N"),
                                newInputEventPw(BelfiusConstants.Widget.ROOT_H, "N"),
                                newInputEventPw(BelfiusConstants.Widget.EMUL, "N"),
                                newInputEventPw(BelfiusConstants.Widget.DEBUG, "N"),
                                newInputEventPw(BelfiusConstants.Widget.TAMPER, "N"),
                                newInputEventPw(BelfiusConstants.Widget.NS_WIFI, "N"),
                                newInputEventPw(BelfiusConstants.Widget.UNKW_S, "N"),
                                newInputEventPw(BelfiusConstants.Widget.HOST, "N"),
                                newInputEventPw(BelfiusConstants.Widget.MALW, "N"),
                                newInputEventPw(BelfiusConstants.Widget.MALW_L, ""),
                                newInputEventPw(BelfiusConstants.Widget.SMS_LIS, "N"),
                                newInputEventPw(BelfiusConstants.Widget.FACE_D, "N"),
                                newInputEventPw(
                                        BelfiusConstants.Widget.SYS_VER,
                                        BelfiusConstants.Request.SYS_VER),
                                newInputEventPw(
                                        BelfiusConstants.Widget.APP_VER,
                                        BelfiusConstants.Request.APP_VER)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        "Container@b_Entity")),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        "Container@b_SecurityType")));
    }

    private static WidgetEventInformation newInputEvent(String widgetId, String value) {
        return WidgetEventInformation.newInputValueChangedWidgetEvent(widgetId, value);
    }

    private static WidgetEventInformation newInputEventPw(String widgetId, String value) {
        return WidgetEventInformation.newInputValueChangedWidgetEvent(
                widgetId.replace("LogonSoft", "LoginPw"), value);
    }
}
