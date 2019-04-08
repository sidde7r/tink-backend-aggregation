package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.BelfiusStringDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BelfiusTransaction {
    // Will match "MAESTRO-BETALING 19/02-MERCHANT NAME BE 15,00   EUR KAART NR 1234 1234 1234 1234
    // - LASTNAME FIRSTNAME   REF. : 123456789 VAL. 20-02
    // Capturing "MERCHANT NAME" in one group, allowing us to parse it out.
    private static Pattern MAESTRO_PURCHASE_FIRST_PART =
            Pattern.compile(
                    "^(MAESTRO-BETALING\\s*\\d\\d/\\d\\d-)(([\\w*\\s])*)(\\s\\w\\w\\s\\d\\d.*$)");

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

    public Transaction toTinkTransaction() {
        Optional<Amount> amount = BelfiusStringUtils.parseStringToAmount(this.amount);

        if (!amount.isPresent()) {
            return null;
        }

        return Transaction.builder()
                .setPending(isPending())
                .setAmount(amount.get())
                .setDescription(getDescription())
                .setRawDetails(getRawDetails())
                .setDate(this.date)
                .build();
    }

    private String getDescription() {
        if (!Strings.isNullOrEmpty(this.nameOppositeSide)) {
            return this.nameOppositeSide;
        }

        if (!Strings.isNullOrEmpty(this.communication)) {
            return this.communication;
        }

        if (!Strings.isNullOrEmpty(this.description)) {
            return getFormattedDescription();
        }

        return null;
    }

    private String getFormattedDescription() {
        // So far only seen Maestro purchases with an empty nameOppositeSide field, which is
        // preferred
        // as description since it's the merchant name without noise. For Maestro purchases we have
        // to parse
        // out the merchant name.
        String trimmedDescriptionWithNewLinesRemoved = this.description.replaceAll("\\n", "");
        Matcher matcher =
                MAESTRO_PURCHASE_FIRST_PART.matcher(
                        trimmedDescriptionWithNewLinesRemoved.toUpperCase());

        if (matcher.matches()) {
            return matcher.group(2);
        }

        return trimmedDescriptionWithNewLinesRemoved;
    }

    @JsonIgnore
    private RawDetails getRawDetails() {
        if (Strings.isNullOrEmpty(this.nameOppositeSide)
                && Strings.isNullOrEmpty(this.description)) {
            return null;
        }

        return new RawDetails(this.nameOppositeSide, this.description);
    }

    @JsonObject
    public class RawDetails {
        private List<String> recipientAccount;
        private List<String> details;

        public RawDetails(String recipientAccount, String details) {
            // be kind no nulls
            this.recipientAccount =
                    !Strings.isNullOrEmpty(recipientAccount)
                            ? Collections.singletonList(recipientAccount)
                            : Collections.emptyList();
            this.details =
                    !Strings.isNullOrEmpty(details)
                            ? Collections.singletonList(details)
                            : Collections.emptyList();
        }
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
