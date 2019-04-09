package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class TransferDestinationAccountEntity extends AbstractAccountEntity
        implements GeneralAccountEntity {
    private List<String> scopes;
    private String currencyCode;
    private String amount;
    private String usageNotification;
    private String id;

    public List<String> getScopes() {
        return scopes;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public String getUsageNotification() {
        return usageNotification;
    }

    public String getId() {
        return id;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(this.fullyFormattedNumber);
    }

    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();
        if (!accountIdentifier.isValid() && !(accountIdentifier instanceof SwedishIdentifier)) {
            return null;
        }

        return ((SwedishIdentifier) accountIdentifier).getBankName();
    }

    @Override
    public String generalGetName() {
        return this.name;
    }

    public boolean scopesContainsIgnoreCase(String scope) {
        return Optional.ofNullable(scopes).orElseGet(Collections::emptyList).stream()
                .anyMatch(listScope -> listScope.equalsIgnoreCase(scope));
    }
}
