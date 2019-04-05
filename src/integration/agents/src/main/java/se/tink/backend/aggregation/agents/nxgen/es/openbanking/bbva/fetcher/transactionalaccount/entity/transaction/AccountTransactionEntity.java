package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.Formats;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountTransactionEntity {

    private String id;
    private String operationDate;
    private double amount;
    private String currency;
    private String description;
    private CategoryEntity category;
    private SubCategoryEntity subCategory;
    private List<ClientNoteEntity> clientNotes = null;
    private List<AttachedInfoEntity> attachedInfo = null;

    @JsonFormat(pattern = Formats.TRANSACTION_DATE_FORMAT)
    private Date valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setExternalId(id)
                .setAmount(new Amount(currency, amount))
                .setDate(valueDate)
                .setDescription(description)
                .build();
    }
}
