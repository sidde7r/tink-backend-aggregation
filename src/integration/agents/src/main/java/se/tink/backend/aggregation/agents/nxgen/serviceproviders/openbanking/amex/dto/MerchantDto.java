package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class MerchantDto {

    private String name;
    private AddressDto address;
}
