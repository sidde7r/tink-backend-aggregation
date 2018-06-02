package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentAccountEntity extends SwedbankAccountEntity {

    private static final AggregationLogger log = new AggregationLogger(PaymentAccountEntity.class);

    private String type;

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        if (Objects.equal(this.type, SwedbankPaymentType.BGACCOUNT.name())) {
            AccountIdentifier bankGiroIdentifier = new BankGiroIdentifier(getAccountNumber());
            bankGiroIdentifier.setName(getName());
            return bankGiroIdentifier;
        } else if (Objects.equal(this.type, SwedbankPaymentType.PGACCOUNT.name())) {
            AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier(getAccountNumber());
            plusGiroIdentifier.setName(getName());
            return plusGiroIdentifier;
        }
        log.error(String.format("Received a unknown payment type %s for account number %s.", this.type,
                getAccountNumber()));
        return new BankGiroIdentifier(null);
    }

    @Override
    public String generalGetBank() {
        return null; // No bank.
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
