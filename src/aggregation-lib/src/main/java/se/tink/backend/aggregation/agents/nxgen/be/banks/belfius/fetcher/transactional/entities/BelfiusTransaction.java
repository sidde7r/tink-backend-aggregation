package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusStringDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class BelfiusTransaction {

    @JsonProperty("lb_Date")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonDeserialize(using = BelfiusDateDeserializer.class)
    private Date date;

    @JsonProperty("lb_AccountOppositeSide")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String accountOppositeSide;

    @JsonProperty("lb_Pending")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String pending;

    @JsonProperty("lb_Amount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String amount;

    @JsonProperty("lb_NameOppositeSide")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String nameOppositeSide;

    @JsonProperty("lb_Communication")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String communication;

    @JsonProperty("lb_Description")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String description;

    public boolean isPending() {
        return "Y".equalsIgnoreCase(this.pending);
    }

    public Amount getAmount() {
        Optional<Amount> amount = BelfiusStringUtils.parseStringToAmount(this.amount);
        return amount.get() == null ? null : amount.get();
    }

    public Transaction toTinkTransaction() {
        Amount amount = getAmount();
        if (amount == null) {
            return null;
        }
        return Transaction.builder()
                .setPending(isPending())
                .setAmount(getAmount())
                .setDescription(this.description)
                .setDate(this.date)
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", this.date)
                .append("accountOppositeSide", this.accountOppositeSide)
                .append("pending", this.pending)
                .append("amount", this.amount)
                .append("nameOppositeSide", this.nameOppositeSide)
                .append("communication", this.communication)
                .append("description", this.description)
                .toString();
    }
}
