package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetailsResponse {
    private String owner;
    private Amount amount;

    @JsonObject
    @Getter
    public static class Amount {
        private BigDecimal creditLine;
    }
}
