package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen;

import java.time.LocalDate;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScope;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.WeightedScopeSupplier;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SparkasenConsentGenerator implements ConsentGenerator<ConsentRequest> {

    private static final ToScope<RefreshableItem, SparkassenScope> ITEM_TO_SCOPE =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                        return SparkassenScope.ACCOUNTS;
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return SparkassenScope.ACCOUNTS_BALANCES_TRANSACTIONS;
                }
            };

    private final WeightedScopeSupplier<RefreshableItem, SparkassenScope> scopeSupplier;
    private final LocalDateTimeSource localDateTimeSource;

    public SparkasenConsentGenerator(
            CredentialsRequest request,
            LocalDateTimeSource localDateTimeSource,
            Set<SparkassenScope> availableScopes) {
        this.localDateTimeSource = localDateTimeSource;
        scopeSupplier =
                new WeightedScopeSupplier<>(
                        ItemsSupplier.get(request), availableScopes, ITEM_TO_SCOPE);
    }

    @Override
    public ConsentRequest generate() {
        LocalDate validUntil = localDateTimeSource.now().toLocalDate().plusDays(90);
        return ConsentRequest.buildTypicalRecurring(
                scopeSupplier.get().createAccessEntity(), validUntil.toString());
    }
}
