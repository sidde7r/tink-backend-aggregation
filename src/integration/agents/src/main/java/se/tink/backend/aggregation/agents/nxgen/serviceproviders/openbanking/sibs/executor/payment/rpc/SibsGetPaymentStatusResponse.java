package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SibsGetPaymentStatusResponse {

    private SibsTransactionStatus transactionStatus;
    private List<SibsTppMessage> tppMessages;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(SibsTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public List<SibsTppMessage> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<SibsTppMessage> tppMessages) {
        this.tppMessages = tppMessages;
    }
}
