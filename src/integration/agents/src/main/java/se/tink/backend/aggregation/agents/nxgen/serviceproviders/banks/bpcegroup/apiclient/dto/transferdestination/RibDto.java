package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class RibDto {

    @JsonProperty("bankid")
    private String bankId;

    private String ribKey;

    private String accountNumber;

    @JsonProperty("deskid")
    private String deskId;
}
