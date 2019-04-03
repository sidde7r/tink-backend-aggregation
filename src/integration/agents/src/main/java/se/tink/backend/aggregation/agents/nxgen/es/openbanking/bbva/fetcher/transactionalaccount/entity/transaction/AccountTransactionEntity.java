
package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountTransactionEntity {

    private String id;
    private String operationDate;
    private String valueDate;
    private double amount;
    private String currency;
    private String description;
    private CategoryEntity category;
    private SubCategoryEntity subCategory;
    private List<ClientNoteEntity> clientNotes = null;
    private List<AttachedInfoEntity> attachedInfo = null;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
            .setExternalId(id)
            .setAmount(new Amount(currency, amount))
            .setDate(getValueDate())
            .setDescription(description)
            .build();
    }

    public Date getValueDate() {
        try {
            return new SimpleDateFormat(BBVAConstants.Formats.TRANSACTION_DATE_FORMAT)
                .parse(valueDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
