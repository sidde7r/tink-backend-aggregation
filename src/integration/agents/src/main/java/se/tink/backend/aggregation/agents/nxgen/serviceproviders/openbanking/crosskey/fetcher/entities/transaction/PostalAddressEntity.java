package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PostalAddressEntity {

    private List<String> addressLine = null;
    private String addressType;
    private String buildingNumber;
    private String country;
    private String countrySubDivision;
    private String department;
    private String postCode;
    private String streetName;
    private String subDepartment;
    private String townName;

}
