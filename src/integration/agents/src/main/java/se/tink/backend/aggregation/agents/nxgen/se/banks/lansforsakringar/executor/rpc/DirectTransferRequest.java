package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DirectTransferRequest {
    private String toText;
    private String fromAccount;
    private BigDecimal amount;
    private String toAccount;
    private String fromText;

    @JsonIgnore
    public DirectTransferRequest(
            String toText,
            String fromAccount,
            BigDecimal amount,
            String toAccount,
            String fromText) {
        this.toText = toText;
        this.fromAccount = fromAccount;
        this.amount = amount;
        this.toAccount = toAccount;
        this.fromText = fromText;
    }
}
