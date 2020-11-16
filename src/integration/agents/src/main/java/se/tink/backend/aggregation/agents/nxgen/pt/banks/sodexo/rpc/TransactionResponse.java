package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class TransactionResponse {

    private List<TransactionEntity> transactions;
}
