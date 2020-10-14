package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import java.util.Date;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class TransactionEntity {

    private String id;
    private Date executionDate;
    private AmountEntity amount;
    private TypeEntity type;
    private String subject;
    private List<String> subjectLines;
}
