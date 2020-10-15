package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class IndividualEntity {

    private String id;
    private String dataSource;
    private String logicalDataDomain;
}
