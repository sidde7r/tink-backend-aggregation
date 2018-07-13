package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentDestinationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransactionAccountGroupEntity;
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
