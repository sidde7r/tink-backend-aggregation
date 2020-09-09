package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(UpperCamelCaseStrategy.class)
public class AddressEntity {
    private boolean isProtectedAddress;
    private String coName;
    private String street;
    private String city;
    private String country;
    private String postalCode;
    private boolean isSwedishAddress;
}
