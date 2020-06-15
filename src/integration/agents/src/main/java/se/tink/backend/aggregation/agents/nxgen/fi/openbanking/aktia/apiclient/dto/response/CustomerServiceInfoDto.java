package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class CustomerServiceInfoDto {

    private String serviceClass;

    private String phone;

    private String openHours;
}
