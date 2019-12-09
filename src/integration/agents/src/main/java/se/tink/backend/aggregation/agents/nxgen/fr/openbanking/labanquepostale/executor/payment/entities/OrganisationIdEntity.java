package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OrganisationIdEntity {
    private String identification;
    private String schemeName;
    private String issuer;
}
