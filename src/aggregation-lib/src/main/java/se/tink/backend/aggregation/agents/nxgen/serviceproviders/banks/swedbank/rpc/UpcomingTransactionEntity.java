package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionEntity {
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private AmountEntity amount;
    private boolean missingSigning;
    private UpcomingTransactionAccount toAccount;
    private UpcomingTransactionAccount fromAccount;
    private LinksEntity links;

    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public boolean isMissingSigning() {
        return missingSigning;
    }

    public UpcomingTransactionAccount getToAccount() {
        return toAccount;
    }

    public UpcomingTransactionAccount getFromAccount() {
        return fromAccount;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public Optional<UpcomingTransaction> toTinkUpcomingTransaction(String defaultCurrency) {
        Preconditions.checkNotNull(defaultCurrency);

        if (date == null || amount == null) {
            return Optional.empty();
        }

        UpcomingTransaction.Builder upcomingTransactionBuilder = UpcomingTransaction.builder()
                .setDate(date)
                .setAmount(amount.toTinkAmount(defaultCurrency));

        return Optional.of(upcomingTransactionBuilder.build());
    }
}
