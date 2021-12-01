package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentDestinationsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.TransactionAccountGroupEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionOptionEntity {
    private PaymentDestinationsEntity payment;
    private List<TransactionAccountGroupEntity> transactionAccountGroups;
}
