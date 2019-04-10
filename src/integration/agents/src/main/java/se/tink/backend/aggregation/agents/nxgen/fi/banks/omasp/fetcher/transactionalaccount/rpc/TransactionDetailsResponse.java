package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionDetailsResponse extends OmaspBaseResponse {
    private String receiverName;
    private String info;
    private AmountEntity sum;
    // `status` is null - cannot define it!
    private DateEntity entryDate;
    private DateEntity valueDate;
    private String accountName;
    private String accountIban;
    private String archiveId;
    private String receiverAccount;
    private String message;
    // `referenceNumber` is null - cannot define it!
    private Boolean copyAllowed;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(sum.getValue()))
                .setDate(entryDate.getValue())
                .setDescription(message)
                .build();
    }
}
