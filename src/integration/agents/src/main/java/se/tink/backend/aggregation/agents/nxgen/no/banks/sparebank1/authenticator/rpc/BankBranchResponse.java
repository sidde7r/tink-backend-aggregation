package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BankBranchResponse {
    private List<Branch> banks;

    @JsonObject
    @Data
    public static class Branch {
        private String id;
        private String name;
    }
}
