package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScope;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccessEntity;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.WeightedScopeSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class RedsysGlobalConsentGenerator implements ConsentGenerator<ConsentRequestBody> {

    private static final ToScope<RefreshableItem, RedsysScope> itemToScope =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case IDENTITY_DATA:
                        return RedsysScope.AVAILABLE_ACCOUNTS_WITH_BALANCES;
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return RedsysScope.ALL_PSD2;
                }
            };

    private final WeightedScopeSupplier<RefreshableItem, RedsysScope> scopeSupplier;
    private final LocalDateTimeSource localDateTimeSource;

    private RedsysGlobalConsentGenerator(
            AgentComponentProvider componentProvider, Set<RedsysScope> availableScopes) {
        this.scopeSupplier =
                new WeightedScopeSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScope);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    public static RedsysGlobalConsentGenerator of(
            AgentComponentProvider componentProvider, Set<RedsysScope> availableScopes) {
        return new RedsysGlobalConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public ConsentRequestBody generate() {
        if (scopeSupplier.get() == RedsysScope.AVAILABLE_ACCOUNTS_WITH_BALANCES) {
            return ConsentRequestBody.builder()
                    .access(AccessEntity.ofAllAccountsWithBalances())
                    .recurringIndicator(false)
                    .validUntil(localDateTimeSource.now().toLocalDate())
                    .frequencyPerDay(RedsysScope.MIN_DAILY_FREQUENCY)
                    .combinedServiceIndicator(false)
                    .build();
        }

        return ConsentRequestBody.builder()
                .access(AccessEntity.ofAllPsd2())
                .recurringIndicator(true)
                .validUntil(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plusDays(RedsysScope.MAX_EXPIRATION_DAYS))
                .frequencyPerDay(RedsysScope.MAX_DAILY_FREQUENCY)
                .combinedServiceIndicator(false)
                .build();
    }
}
