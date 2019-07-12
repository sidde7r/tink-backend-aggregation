package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddressEntity {
    private String addressLine;
    private String country;
}
