package se.tink.backend.aggregation.agents.consent.generators.nlob.abnamro;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AbnAmroConsentGenerator implements ConsentGenerator<String> {

    private final ScopesSupplier<RefreshableItem, AbnAmroScope> scopesSupplier;

    private static final ToScopes<RefreshableItem, AbnAmroScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                        return Sets.newHashSet(
                                AbnAmroScope.READ_ACCOUNTS, AbnAmroScope.READ_ACCOUNTS_DETAILS);
                    case CHECKING_TRANSACTIONS:
                        return Sets.newHashSet(AbnAmroScope.READ_TRANSACTIONS_HISTORY);
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

    public AbnAmroConsentGenerator(CredentialsRequest request, Set<AbnAmroScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(ItemsSupplier.get(request), availableScopes, itemToScopes);
    }

    public static AbnAmroConsentGenerator of(
            CredentialsRequest request, Set<AbnAmroScope> availableScopes) {
        return new AbnAmroConsentGenerator(request, availableScopes);
    }

    @Override
    public String generate() {
        return String.join(" ", scopesSupplier.getStrings());
    }
}
