package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorPostalAddress {
    private String streetName;
    private String countrySubDivision;
    private String department;
    private List<String> addressLine;
    private String buildingNumber;
    private String townName;
    private String country;
    private String subDepartment;
    private String addressType;
    private String postCode;
}
