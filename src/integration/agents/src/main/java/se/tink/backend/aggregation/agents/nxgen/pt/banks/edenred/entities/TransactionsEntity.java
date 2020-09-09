package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities;

import java.util.List;
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
public class TransactionsEntity {

    private AccountEntity account;
    private List<TransactionEntity> movementList;
}
