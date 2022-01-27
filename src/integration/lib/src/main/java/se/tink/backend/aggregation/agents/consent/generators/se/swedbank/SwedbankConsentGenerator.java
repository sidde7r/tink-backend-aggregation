package se.tink.backend.aggregation.agents.consent.generators.se.swedbank;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class SwedbankConsentGenerator implements ConsentGenerator<String> {

    private final ScopesSupplier<RefreshableItem, SwedbankScope> scopesSupplier;

    private static final ToScopes<RefreshableItem, SwedbankScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                        return Sets.newHashSet(
                                SwedbankScope.READ_TRANSACTIONS_HISTORY,
                                SwedbankScope.READ_TRANSACTIONS_HISTORY_OVER90);
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                        return Sets.newHashSet(SwedbankScope.READ_ACCOUNTS_BALANCES);
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
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

    public SwedbankConsentGenerator(
            CredentialsRequest request, Set<SwedbankScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(ItemsSupplier.get(request), availableScopes, itemToScopes);
    }

    public static SwedbankConsentGenerator of(
            CredentialsRequest request, Set<SwedbankScope> availableScopes) {
        return new SwedbankConsentGenerator(request, availableScopes);
    }

    public List<String> withObligatoryScopes() {
        List<String> withObligatoryScopes = new ArrayList<>();
        withObligatoryScopes.add(SwedbankScope.PSD2.toString());
        withObligatoryScopes.addAll(scopesSupplier.getStrings());
        return withObligatoryScopes;
    }

    @Override
    public String generate() {
        log.info(
                "[CONSENT GENERATOR] scopes generated with obligatory scopes: {}",
                withObligatoryScopes());
        return String.join(" ", withObligatoryScopes());
    }
}
