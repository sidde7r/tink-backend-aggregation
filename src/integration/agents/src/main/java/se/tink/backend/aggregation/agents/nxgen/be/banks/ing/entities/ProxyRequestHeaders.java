package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
@JsonInclude(Include.NON_NULL)
public class ProxyRequestHeaders {

    @JsonProperty("X-ING-SecureRemotePassword-Version")
    private String secureRemotePasswordVersion;

    private String accept;

    @JsonProperty("Accept-Language")
    private String acceptLanguage;

    @JsonProperty("X-ING-APP_VERSION")
    private String appVersion;

    @JsonProperty("X-ING-OS_VERSION")
    private String osVersion;

    @JsonProperty("X-ING-DEVICE_PLATFORM")
    private String devicePlatform;

    @JsonProperty("X-ING-APP_IDENTIFIER")
    private String appIdentifier;

    @JsonProperty("X-ING-DEVICE_MODEL")
    private String deviceModel;
}
