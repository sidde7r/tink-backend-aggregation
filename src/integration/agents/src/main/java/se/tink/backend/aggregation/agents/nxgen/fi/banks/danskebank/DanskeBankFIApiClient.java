package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import com.google.common.base.Strings;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankFIApiClient extends DanskeBankApiClient {
    private static final AggregationLogger log = new AggregationLogger(DanskeBankFIApiClient.class);
    private final DanskeBankFIConfiguration configuration;
    protected DanskeBankFIApiClient(TinkHttpClient client, DanskeBankFIConfiguration configuration) {
        super(client, configuration);
        this.configuration = configuration;
    }

    public HttpResponse collectDynamicChallengeJavascript() {
        return client.request(DanskeBankConstants.Url.DYNAMIC_JS_AUTHORIZE)
                .header("Referer", configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    public ListOtpResponse listOtpInformation(ListOtpRequest request) {
        String response = client.request(DanskeBankConstants.Url.DEVICE_LIST_OTP)
                .header("Referer", configuration.getAppReferer())
                .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListOtpResponse.class);
    }

    public CheckDeviceResponse checkDevice(String deviceSerialNumberValue, String stepUpTokenValue) {
        RequestBuilder requestBuilder = client.request(DanskeBankConstants.Url.DEVICE_BIND_CHECK)
                .header("Referer", configuration.getAppReferer())
                .header(configuration.getDeviceSerialNumberKey(), deviceSerialNumberValue);

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, new JSONObject().toString());

        CheckDeviceResponse checkDeviceResponse = DanskeBankDeserializer
                .convertStringToObject(response, CheckDeviceResponse.class);
        if (checkDeviceResponse.getError() != null) {
            log.info(
                    String.format(
                            "DanskeBank - Found non null error in check device response - response: [%s]", response));
        }

        return checkDeviceResponse;
    }

    public BindDeviceResponse bindDevice(String stepUpTokenValue, BindDeviceRequest request) {
        RequestBuilder requestBuilder = client.request(DanskeBankConstants.Url.DEVICE_BIND_BIND)
                .header("Referer", configuration.getAppReferer());

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, BindDeviceResponse.class);
    }
}
