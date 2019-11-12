package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Request;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfoEntity {
    @JsonProperty
    private String platformName = SpankkiConstants.DEVICE_PROFILE.getOs().toLowerCase();

    @JsonProperty private String appVersion = Request.CLIENT_INFO_APP_VERSION;
    @JsonProperty private String lang = Request.CLIENT_INFO_LANG;

    @JsonProperty
    private String platformType = SpankkiConstants.DEVICE_PROFILE.getOs().toLowerCase();

    @JsonProperty private String appName = Request.CLIENT_INFO_APP_NAME;
    @JsonProperty private String platformVersion = SpankkiConstants.DEVICE_PROFILE.getOsVersion();
    @JsonProperty private String deviceModel = SpankkiConstants.DEVICE_PROFILE.getPhoneModel();
}
