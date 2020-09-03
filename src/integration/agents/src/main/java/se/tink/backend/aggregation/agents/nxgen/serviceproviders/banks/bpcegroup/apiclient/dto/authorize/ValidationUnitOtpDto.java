package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class ValidationUnitOtpDto implements ValidationUnitRequestItemBaseDto {

    private String id;

    @JsonProperty("otp_sms")
    private String otpCode;

    @Builder.Default private String type = "SMS";
}
