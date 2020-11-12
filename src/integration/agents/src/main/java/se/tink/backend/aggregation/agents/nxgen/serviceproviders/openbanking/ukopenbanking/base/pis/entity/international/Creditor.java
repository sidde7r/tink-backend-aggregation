package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Creditor {
    private PostalAddress postalAddress;
    private String name;
}
