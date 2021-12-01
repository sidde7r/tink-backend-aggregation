package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransferTransactionEntity {
    private String currencyCode;
    private String amount;
    private List<TransactionEntity> transactions;
    private FromAccountEntity fromAccount;
}
