package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeliveryAddress {
    private String streetName;
    private List<String> countrySubDivision;
    private List<String> addressLine;
    private String buildingNumber;
    private String townName;
    private String country;
    private String postCode;
}
