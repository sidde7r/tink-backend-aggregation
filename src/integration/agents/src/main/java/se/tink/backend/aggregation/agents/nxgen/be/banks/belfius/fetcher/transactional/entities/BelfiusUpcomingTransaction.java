package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusStringDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

import java.util.Date;
import java.util.Optional;

@JsonObject
public class BelfiusUpcomingTransaction {

    @JsonProperty("lb_DateExec")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonDeserialize(using = BelfiusDateDeserializer.class)
    private Date date;

    @JsonProperty("lb_BenefAccount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String accountOppositeSide;

    @JsonProperty("lb_Amount")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String amount;

    @JsonProperty("lb_BenefName")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String beneficiary;

    @JsonProperty("mlb_Communication")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String communication;

    @JsonProperty("lb_Test_Description")
    @JsonDeserialize(using = BelfiusStringDeserializer.class)
    private String description;

    public UpcomingTransaction toTinkUpcomingTransaction() {
        Optional<Amount> amount = BelfiusStringUtils.parseStringToAmount(this.amount);

        return amount.map(amount1 -> UpcomingTransaction.builder()
                .setAmount(amount1)
                .setDate(date)
                .setDescription(getDescription())
                .build())
                .orElse(null);

    }

    private String getDescription() {
        if (!Strings.isNullOrEmpty(this.beneficiary)) {
            return this.beneficiary;
        }

        if (!Strings.isNullOrEmpty(this.communication)) {
            return this.communication;
        }

        if (!Strings.isNullOrEmpty(this.description)) {
            return description;
        }

        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("accountOppositeSide", accountOppositeSide)
                .append("amount", amount)
                .append("beneficiary", beneficiary)
                .append("communication", communication)
                .append("description", description)
                .build();
    }
}
