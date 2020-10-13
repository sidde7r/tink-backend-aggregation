package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@NoArgsConstructor
public class AliasEntity {

    private String type;
    private String value;
    private String realmId;

    public AliasEntity(String tppId, String realmId) {
        this.type = "EIDAS";
        this.value = tppId;
        this.realmId = realmId;
    }
}
