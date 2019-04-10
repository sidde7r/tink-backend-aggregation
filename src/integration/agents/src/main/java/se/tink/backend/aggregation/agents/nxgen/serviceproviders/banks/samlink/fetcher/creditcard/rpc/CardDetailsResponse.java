package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardDetailsResponse {

    private AmountEntity availableCredit;
    private AmountEntity
            creditLimit; // the app claims it could be present, I haven't seen it so far.

    //    Fields probably unnecessary to keep:
    private String cardId;
    private String name;
    private String type;
    private String number;

    @JsonFormat(pattern = "M/y")
    private Date expiration;

    public Optional<Amount> calculateBalance(Consumer<String> logger) {
        if (this.creditLimit == null || this.availableCredit == null) {
            return Optional.empty();
        }
        logger.accept("Found an actual credit card!");
        return Optional.of(
                Amount.inEUR(this.creditLimit.getAmount() - this.availableCredit.getAmount()));
    }
}
