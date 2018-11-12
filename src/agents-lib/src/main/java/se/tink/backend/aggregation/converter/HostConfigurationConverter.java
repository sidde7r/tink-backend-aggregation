package se.tink.backend.aggregation.converter;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.api.CallbackHostConfiguration;

import java.util.Base64;

public class HostConfigurationConverter {

    public static HostConfiguration convert(CallbackHostConfiguration callbackHostConfiguration){
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId(callbackHostConfiguration.getClusterId());
        hostConfiguration.setHost(callbackHostConfiguration.getHost());
        hostConfiguration.setApiToken(callbackHostConfiguration.getApiToken());
        hostConfiguration.setBase64encodedclientcert(callbackHostConfiguration.getBase64encodedclientcert());
        hostConfiguration.setDisablerequestcompression(callbackHostConfiguration.isDisablerequestcompression());
        return hostConfiguration;
    }
}
