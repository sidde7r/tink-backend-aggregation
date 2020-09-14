package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoansEntity extends StandardResponse {
    private boolean error;
    private String loanStatus;
    private List<PropertiesEntity> properties;
    private List<SBABLoansEntity> privateLoans;
}
