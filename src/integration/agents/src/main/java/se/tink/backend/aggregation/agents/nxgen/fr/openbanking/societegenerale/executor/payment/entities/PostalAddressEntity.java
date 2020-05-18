package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PostalAddressEntity {
    private String PostalAddressEntity;
    private List<String> addressLine;
}
