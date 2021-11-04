package se.tink.backend.aggregation.agents.consent.generators.nl.ing;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IngConsentGenerator implements ConsentGenerator<String> {

    private static final ToScopes<RefreshableItem, IngScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                        return Sets.newHashSet(IngScope.VIEW_PAYMENT_BALANCES);
                    case CHECKING_TRANSACTIONS:
                        return Sets.newHashSet(IngScope.VIEW_PAYMENT_TRANSACTIONS);
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, IngScope> scopesSupplier;

    public IngConsentGenerator(CredentialsRequest request, Set<IngScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(ItemsSupplier.get(request), availableScopes, itemToScopes);
    }

    public static IngConsentGenerator of(
            CredentialsRequest request, Set<IngScope> availableScopes) {
        return new IngConsentGenerator(request, availableScopes);
    }

    @Override
    public String generate() {
        return String.join(" ", scopesSupplier.getStrings());
    }
}
