package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

@JsonObject
public class AccountHolderEntity {

    @JsonProperty("nombreInterviniente")
    private String holderName;

    @JsonProperty("numOrdenInterviniente")
    private String holderOrder;

    @JsonProperty("relacionAcuerdoPersona")
    private String holderType;

    public String getHolderName() {
        return holderName;
    }

    public String getHolderOrder() {
        return holderOrder;
    }

    public String getHolderType() {
        return holderType;
    }

    public Party toTinkParty() {
        return new Party(holderName, "01".equals(holderType) ? Role.HOLDER : Role.AUTHORIZED_USER);
    }
}
