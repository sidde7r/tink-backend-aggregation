package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountApiIdentifiersResponse {
    private List<Accounts> accounts;

    @JsonObject
    @Data
    public static class Accounts {
        private String key;
        private String accountNumber;
    }
}
