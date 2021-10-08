package se.tink.backend.aggregation.agents.consent.generators.fi.opbank;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class OpBankConsentGenerator implements ConsentGenerator<Set<String>> {

    private static final ToScopes<RefreshableItem, OpBankScope> itemToScopes =
            item -> {
                switch (item) {
                    case IDENTITY_DATA:
                        return Sets.newHashSet(OpBankScope.OPENID, OpBankScope.VERIFY_ACCOUNTS);
                    case SAVING_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case CHECKING_ACCOUNTS:
                        return Sets.newHashSet(OpBankScope.OPENID, OpBankScope.READ_ACCOUNTS);
                    case CREDITCARD_ACCOUNTS:
                        return Sets.newHashSet(
                                OpBankScope.OPENID,
                                OpBankScope.READ_ACCOUNTS,
                                OpBankScope.READ_CARDS);
                    case SAVING_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case CHECKING_TRANSACTIONS:
                        return Sets.newHashSet(
                                OpBankScope.OPENID,
                                OpBankScope.READ_ACCOUNTS_TRANSACTIONS,
                                OpBankScope.READ_ACCOUNTS_TRANSACTIONS_HISTORY);
                    case CREDITCARD_TRANSACTIONS:
                        return Sets.newHashSet(
                                OpBankScope.OPENID,
                                OpBankScope.READ_CARDS_TRANSACTIONS,
                                OpBankScope.READ_CARDS_TRANSACTIONS_HISTORY);
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, OpBankScope> scopesSupplier;

    public OpBankConsentGenerator(
            AgentComponentProvider componentProvider, Set<OpBankScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static OpBankConsentGenerator of(
            AgentComponentProvider componentProvider, Set<OpBankScope> availableScopes) {
        return new OpBankConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public Set<String> generate() {
        return scopesSupplier.getStrings();
    }
}
