package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;

@Data
public class TransactionEntity {

    private String description;
    private String date;

    @JsonProperty("date_iso_8601")
    private Date dateIso;

    private int type;
    private double amount;
}
