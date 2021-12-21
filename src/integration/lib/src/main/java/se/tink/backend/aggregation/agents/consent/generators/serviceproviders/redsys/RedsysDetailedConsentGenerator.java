package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import static se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope.ACCOUNTS;
import static se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope.BALANCES;
import static se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope.TRANSACTIONS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysDetailedConsentGeneratorBuilder.Steps;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccessEntity;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class RedsysDetailedConsentGenerator implements ConsentGenerator<ConsentRequestBody> {

    private static final ToScopes<RefreshableItem, RedsysScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                        return EnumSet.of(ACCOUNTS, BALANCES);
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                        return EnumSet.of(TRANSACTIONS);
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };
    private static final ZoneId SPAIN_ZONE_ID = ZoneId.of("Europe/Madrid");

    private final ScopesSupplier<RefreshableItem, RedsysScope> scopesProvider;
    private final List<AccountInfoEntity> accountInfoEntities;
    private final LocalDateTimeSource localDateTimeSource;

    RedsysDetailedConsentGenerator(
            AgentComponentProvider componentProvider,
            Set<RedsysScope> availableScopes,
            List<AccountInfoEntity> accountInfoEntities) {
        this.scopesProvider =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
        this.accountInfoEntities = accountInfoEntities;
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    public static Steps builder() {
        return new Steps();
    }

    @Override
    public ConsentRequestBody generate() {
        int daysUntilExpiration = RedsysScope.MIN_EXPIRATION_DAYS;
        int frequencyPerDay = RedsysScope.MIN_DAILY_FREQUENCY;
        boolean recurringIndicator = false;

        Set<RedsysScope> scopes = scopesProvider.get();
        AccessEntity accessEntity = new AccessEntity();
        if (scopes.contains(ACCOUNTS)) {
            accessEntity.setAccounts(accountInfoEntities);
        }

        if (scopes.contains(BALANCES)) {
            accessEntity.setBalances(accountInfoEntities);
        }

        if (scopes.contains(TRANSACTIONS)) {
            accessEntity.setTransactions(accountInfoEntities);
            daysUntilExpiration = RedsysScope.MAX_EXPIRATION_DAYS;
            frequencyPerDay = RedsysScope.MAX_DAILY_FREQUENCY;
            recurringIndicator = true;
        }

        LocalDateTime nowMadrid = localDateTimeSource.now(SPAIN_ZONE_ID);
        LocalDateTime nowLocal = localDateTimeSource.now();
        LocalDate validUntil = nowMadrid.toLocalDate().plusDays(daysUntilExpiration);
        log.info(
                String.format(
                        "Consent validUntil debug: NOW (local): %s; NOW (Madrid): %s; validUntil: %s",
                        nowLocal, nowMadrid, validUntil));
        return ConsentRequestBody.builder()
                .access(accessEntity)
                .recurringIndicator(recurringIndicator)
                .validUntil(validUntil)
                .frequencyPerDay(frequencyPerDay)
                .combinedServiceIndicator(true)
                .build();
    }
}
