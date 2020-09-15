package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class TransactionEntity {

    private Date transactionDate;
    private long transactionType;
    private String transactionName;
    private double amount;
    private String mcc;
    private CategoryEntity category;
    private double balance;
}
