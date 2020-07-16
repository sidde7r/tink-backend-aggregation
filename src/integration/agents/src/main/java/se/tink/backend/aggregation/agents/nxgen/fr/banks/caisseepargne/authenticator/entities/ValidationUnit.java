package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationUnit {
    @JsonProperty("password")
    private String password;

    @JsonProperty("otp_sms")
    private String otpSms;

    @JsonProperty("virtualKeyboard")
    private VirtualKeyboard virtualKeyboard;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;
}
