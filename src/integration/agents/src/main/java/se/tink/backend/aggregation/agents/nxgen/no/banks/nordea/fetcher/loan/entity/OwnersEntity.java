package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
@Getter
public class OwnersEntity {
    private String name;
}
