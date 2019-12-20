package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentDestinationsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.TransactionAccountGroupEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionOptionEntity {
    private PaymentDestinationsEntity payment;
    private List<TransactionAccountGroupEntity> transactionAccountGroups;

    public PaymentDestinationsEntity getPayment() {
        return payment;
    }

    public List<TransactionAccountGroupEntity> getTransactionAccountGroups() {
        return transactionAccountGroups;
    }
}
