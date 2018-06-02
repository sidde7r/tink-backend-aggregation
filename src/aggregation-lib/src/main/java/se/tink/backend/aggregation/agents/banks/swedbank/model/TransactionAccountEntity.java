package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAccountEntity extends SwedbankAccountEntity {
    private Set<AccountScope> scopes;
    private String amount;
    private String clearingNumber;
    private String fullyFormattedNumber;

    public Set<AccountScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<AccountScope> scopes) {
        this.scopes = scopes != null ? Sets.filter(scopes, Predicates.notNull()) : null;
    }

    public String getAmount() {
        return amount;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }

    public boolean hasScopeInScopeList(AccountScope accountScope) {
        return scopes.stream().anyMatch(currScope -> Objects.equal(currScope, accountScope));
    }

    public enum AccountScope {
        TRANSFER_FROM, TRANSFER_TO, PAYMENT_FROM;

        @JsonCreator
        public static AccountScope forValue(String accountScopeName) {
            try {
                return valueOf(accountScopeName);
            } catch (IllegalArgumentException notFound) {
                return null;
            }
        }

        @JsonValue
        public String toValue() {
            return name();
        }

        @JsonValue
        public String toString() {
            return name();
        }
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getFullyFormattedNumber());
    }

    @Override
    public String generalGetBank() {
        return generalGetAccountIdentifier().isValid() ?
                generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName() : null;
    }
}
