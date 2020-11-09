package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.entities.TransactionEntity;

@Data
public class TransactionResponse {

    private List<TransactionEntity> transactions;
}
