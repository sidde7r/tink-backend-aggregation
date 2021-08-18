package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class IdEntity {

    private String regNo;
    private String accountNo;

    public String getBban() {
        return regNo + accountNo;
    }
}
