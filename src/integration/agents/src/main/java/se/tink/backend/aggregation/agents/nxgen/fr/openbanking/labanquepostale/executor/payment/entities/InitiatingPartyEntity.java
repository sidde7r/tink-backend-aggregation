package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiatingPartyEntity {
    private String name;
    private PostalAddressEntity postalAddress;
    private OrganisationIdEntity organisationId;
}
