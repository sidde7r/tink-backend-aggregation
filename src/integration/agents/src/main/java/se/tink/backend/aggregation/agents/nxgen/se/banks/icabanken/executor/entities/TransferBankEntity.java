package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferBankEntity {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("TransferBankId")
    private String transferBankId;

    public String getName() {
        return name;
    }

    public String getTransferBankId() {
        return Optional.ofNullable(transferBankId).orElse("-1");
    }
}
