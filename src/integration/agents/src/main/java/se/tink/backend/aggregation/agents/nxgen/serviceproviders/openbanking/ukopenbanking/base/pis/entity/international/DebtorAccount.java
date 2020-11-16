package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorAccount {
    private String secondaryIdentification;
    private String schemeName;
    private String identification;
    private String name;
}
