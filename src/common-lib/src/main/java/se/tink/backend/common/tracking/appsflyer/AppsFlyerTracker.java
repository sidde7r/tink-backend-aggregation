package se.tink.backend.common.tracking.appsflyer;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.net.LightweightHttpRequest;
import se.tink.libraries.net.LightweightHttpRequestFactory;

public class AppsFlyerTracker {

    private static final String DEV_KEY = "2PEBqyDDXwfqBBZobbErQn";
    private static final String IOS_APP_ID = "id649395387";
    private static final String ANDROID_APP_ID = "se.tink.android";

    private static final String HEADER_AUTHENTICATION = "authentication";
    private static final String BASE_URL = "http://api2.appsflyer.com/";
    private static final String PATH = "inappevent/";
    private static final LogUtils log = new LogUtils(AppsFlyerTracker.class);

    private Map<String, String> headers;

    private String baseUrl;

    @Inject
    public AppsFlyerTracker() {
        baseUrl = BASE_URL + PATH;
        headers = createHeaders();
    }

    private static Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HEADER_AUTHENTICATION, DEV_KEY);
        return headers;
    }

    private static JSONObject createBody(String appsFlyerDeviceId, String name, String value, String ip) {
        JSONObject json;
        try {
            json = new JSONObject().put("appsflyer_id", appsFlyerDeviceId).put("eventName", name)
                    .put("eventValue", (value != null ? value : ""));

            if (ip != null) {
                json.put("ip", ip);
            }

            return json;
        } catch (JSONException e) {
            log.error("Couldn't create JSONObject", e);
        }
        return null;
    }

    private String getAppSpecificPath(String deviceType) {
        if ("android".equals(deviceType)) {
            return ANDROID_APP_ID;
        } else if ("ios".equals(deviceType)) {
            return IOS_APP_ID;
        } else {
            throw new IllegalArgumentException("Only tracks events for \"ios\" and \"android\".");
        }
    }

    public void trackEvent(AppsFlyerEvent event) {
        String url = baseUrl + getAppSpecificPath(event.getDeviceType());
        LightweightHttpRequest request = LightweightHttpRequestFactory.create(url, headers);
        JSONObject json = createBody(event.getAppsFlyerDeviceId(), event.getName(), event.getValue(), event.getIp());
        request.post(json);
    }
}
