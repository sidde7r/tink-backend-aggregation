package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.StringCleaningDeserializer;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonObject
public class HandelsbankenSEPaymentDetailRecipient {

    static final Pattern PATTERN_BG_RECIPIENT = Pattern.compile(".*\\d{3,4}-\\d{4}");
    static final Pattern PATTERN_PG_RECIPIENT = Pattern.compile(".*\\d{1,7}-\\d");

    @JsonDeserialize(using = StringCleaningDeserializer.class)
    private String name;
    private String reference;
    private String id;

    public void applyTo(Transfer transfer) {
        transfer.setDestination(generateAccountIdentifier());
        transfer.setSourceMessage(name);
    }

    private AccountIdentifier generateAccountIdentifier() {
        if (isBGRecipient()) {
            AccountIdentifier bankGiroIdentifier = new BankGiroIdentifier(this.id);
            bankGiroIdentifier.setName(name);
            return bankGiroIdentifier;
        }

        if (isPGRecipient()) {
            AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier(this.id);
            plusGiroIdentifier.setName(name);
            return plusGiroIdentifier;
        }

        return new NonValidIdentifier(this.id);
    }

    private boolean isBGRecipient() {
        Matcher matcher = PATTERN_BG_RECIPIENT.matcher(reference);
        return matcher.matches();
    }

    private boolean isPGRecipient() {
        Matcher matcher = PATTERN_PG_RECIPIENT.matcher(reference);
        return matcher.matches();
    }
}
