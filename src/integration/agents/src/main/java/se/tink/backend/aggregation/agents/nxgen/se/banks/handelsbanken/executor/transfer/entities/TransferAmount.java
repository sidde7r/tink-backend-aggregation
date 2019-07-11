package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferAmount {

    private String number;
    private String numberFormatted;
    private String name;

    private TransferAmount() {}

    @JsonIgnore
    public static TransferAmount from(String number, String numberFormatted, String name) {
        TransferAmount transferAmount = new TransferAmount();
        transferAmount.number = number;
        transferAmount.numberFormatted = numberFormatted;
        transferAmount.name = name;
        return transferAmount;
    }
}
