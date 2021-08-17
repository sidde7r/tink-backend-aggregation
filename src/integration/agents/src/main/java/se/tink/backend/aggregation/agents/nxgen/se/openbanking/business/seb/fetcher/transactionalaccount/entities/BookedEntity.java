package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.SebSEBusinessApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {
    private String transactionId;

    private String valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String entryReference;

    private String descriptiveText;

    private TransactionAmountEntity transactionAmount;

    private int proprietaryBankTransactionCode;

    private String proprietaryBankTransactionCodeText;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public Transaction toTinkTransaction(SebSEBusinessApiClient apiClient) {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(bookingDate)
                .setDescription(getDescription(apiClient))
                .setPending(false)
                .build();
    }

    @JsonIgnore
    private String getDescription(SebSEBusinessApiClient apiClient) {
        // In case of Foreign transactions we don't get good enough information in 'descriptiveText'
        if (proprietaryBankTransactionCode == 10 && !Objects.isNull(links)) {
            TransactionDetailsEntity detailsEntity =
                    apiClient.fetchTransactionDetails(links.getTransactions().getHref());
            return detailsEntity.getCardAcceptorId();
        }
        return descriptiveText;
    }
}
