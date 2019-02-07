package se.tink.libraries.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionContainerEntity {
    private final static Logger log = LoggerFactory.getLogger(TransactionContainerEntity.class);
    private final static ImmutableSet<String> PENDING_TYPES = ImmutableSet.of("AUTHORIZATION");
    private final static String EUR_CURRENCY_CODE = "EUR";

    private TransactionEntity creditCardTransaction;

    public TransactionEntity getCreditCardTransaction() {
        return creditCardTransaction;
    }

    public void setCreditCardTransaction(TransactionEntity creditCardTransaction) {
        this.creditCardTransaction = creditCardTransaction;
    }

    /**
     * Return the amount on the transaction. The amount that we get from ABN is inverted.
     */
    public Double getAmount() {

        if (creditCardTransaction.getBillingAmount() == null) {
            log.warn("No billing amount on credit card transaction.");
            return 0D;
        }

        return -creditCardTransaction.getBillingAmount().getAmount();
    }

    @JsonIgnore
    public boolean isInEUR() {
        return creditCardTransaction != null && creditCardTransaction.getBillingAmount() != null &&
                EUR_CURRENCY_CODE.equalsIgnoreCase(creditCardTransaction.getBillingAmount().getCurrencyCode());
    }

    public boolean isPending() {
        return PENDING_TYPES.contains(creditCardTransaction.getType());
    }
}
