package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

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
public class ProxyResponseHeaders {

    @JsonProperty("x-ing-tracingid")
    private String tracingId;

    @JsonProperty("X-ING-Response-ID")
    private String responseId;

    @JsonProperty("Content-Security-Policy")
    private String contentSecurityPolicy;

    @JsonProperty("Strict-Transport-Security")
    private String strictTransportSecurity;

    @JsonProperty("X-Content-Type-Options")
    private String contentTypeOptions;

    @JsonProperty("X-Frame-Options")
    private String frameOptions;

    @JsonProperty("X-XSS-Protection")
    private String xssProtection;

    @JsonProperty("Cache-Control")
    private String cacheControl;

    @JsonProperty("Pragma")
    private String pragma;

    @JsonProperty("Expires")
    private String expires;
}
