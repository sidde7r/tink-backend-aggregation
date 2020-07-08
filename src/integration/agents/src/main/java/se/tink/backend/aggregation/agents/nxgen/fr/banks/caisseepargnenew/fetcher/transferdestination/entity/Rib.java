package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Rib {
    @JsonProperty("bankid")
    private String bankid;

    @JsonProperty("ribKey")
    private String ribKey;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("deskid")
    private String deskid;
}
