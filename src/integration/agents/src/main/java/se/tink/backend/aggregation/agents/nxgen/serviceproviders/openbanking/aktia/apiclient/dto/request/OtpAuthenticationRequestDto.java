package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.request;

import lombok.Value;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Value
public class OtpAuthenticationRequestDto {

    private String otpCode;
}
