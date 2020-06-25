package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansEntity extends StandardResponse {
    private boolean error;
    private String loanStatus;
    private List<PropertiesEntity> properties;

    public boolean isError() {
        return error;
    }

    public String getLoanStatus() {
        return loanStatus;
    }

    public List<PropertiesEntity> getProperties() {
        return properties;
    }
}
