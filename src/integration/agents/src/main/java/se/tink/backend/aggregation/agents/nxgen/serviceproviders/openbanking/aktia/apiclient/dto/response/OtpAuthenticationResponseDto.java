package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpAuthenticationResponseDto {

    private boolean otpAccepted;

    private OtpInfoDto otpInfo;
}
