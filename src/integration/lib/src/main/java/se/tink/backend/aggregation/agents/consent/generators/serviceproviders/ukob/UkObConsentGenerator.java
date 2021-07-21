package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class UkObConsentGenerator implements ConsentGenerator<AccountPermissionRequest> {

    private static final ToScopes<RefreshableItem, UkObScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                        return Sets.newHashSet(
                                UkObScope.READ_ACCOUNTS_DETAIL,
                                UkObScope.READ_BALANCES,
                                UkObScope.READ_PARTY);
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                        return Sets.newHashSet(
                                UkObScope.READ_TRANSACTIONS_DETAIL,
                                UkObScope.READ_TRANSACTIONS_DEBITS,
                                UkObScope.READ_TRANSACTIONS_CREDITS);
                    case IDENTITY_DATA:
                        return Sets.newHashSet(UkObScope.READ_PARTY_PSU);
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                        return Sets.newHashSet(UkObScope.READ_BENEFICIARIES_DETAIL);
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, UkObScope> scopesSupplier;

    private UkObConsentGenerator(
            AgentComponentProvider componentProvider, Set<UkObScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static UkObConsentGenerator of(
            AgentComponentProvider componentProvider, Set<UkObScope> availableScopes) {
        return new UkObConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public AccountPermissionRequest generate() {
        return AccountPermissionRequest.of(scopesSupplier.getStrings());
    }
}
