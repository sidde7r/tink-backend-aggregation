package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpInfoDto {

    private String nextOtpIndex;

    private String currentOtpCard;

    private boolean fixedOtpCard;

    private String nextOtpCard;
}
