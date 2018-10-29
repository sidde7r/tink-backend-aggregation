package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Notifications {

    @JsonProperty("card_validity")
    private boolean cardValidity;

    @JsonProperty("einvoice_in")
    private boolean einvoiceIn;

    @JsonProperty("payment_pending")
    private boolean paymentPending;

    @JsonProperty("payment_in")
    private boolean paymentIn;

    @JsonProperty("payment_fail")
    private boolean paymentFail;

    @JsonProperty("account_limit_reached")
    private boolean accountLimitReached;

    public boolean isCardValidity() {
        return cardValidity;
    }

    public boolean isEinvoiceIn() {
        return einvoiceIn;
    }

    public boolean isPaymentPending() {
        return paymentPending;
    }

    public boolean isPaymentIn() {
        return paymentIn;
    }

    public boolean isPaymentFail() {
        return paymentFail;
    }

    public boolean isAccountLimitReached() {
        return accountLimitReached;
    }
}
