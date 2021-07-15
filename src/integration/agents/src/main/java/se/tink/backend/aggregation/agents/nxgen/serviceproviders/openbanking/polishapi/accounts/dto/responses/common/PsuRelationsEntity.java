package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PsuRelationsEntity {
    private String typeOfRelation;
    private String typeOfProxy;
    private int stake;
}
