package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericIdentificationEntity {
    private String identification;
    private String schemeName;
    private String issuer;
}
