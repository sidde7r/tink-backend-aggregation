package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SparkassenConsentGeneratorTest {

    private static final LocalDateTimeSource LOCAL_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();

    private SparkassenConsentGenerator sparkassenConsentGenerator;

    @Test
    public void shouldGenerateAvailableAccountsWithBalanceEvenForAccounts() {
        sparkassenConsentGenerator =
                createGenerator(
                        EnumSet.of(
                                RefreshableItem.CHECKING_ACCOUNTS,
                                RefreshableItem.CREDITCARD_ACCOUNTS));

        ConsentRequest consentRequest = sparkassenConsentGenerator.generate();

        assertThat(consentRequest)
                .isEqualTo(
                        ConsentRequest.buildTypicalRecurring(
                                AccessEntity.builder()
                                        .availableAccountsWithBalance(AccessType.ALL_ACCOUNTS)
                                        .build(),
                                LOCAL_DATE_TIME_SOURCE));
    }

    @Test
    public void shouldGenerateAvailableAccountsWithBalance() {
        sparkassenConsentGenerator =
                createGenerator(
                        EnumSet.of(
                                RefreshableItem.CHECKING_ACCOUNTS,
                                RefreshableItem.LOAN_TRANSACTIONS));

        ConsentRequest consentRequest = sparkassenConsentGenerator.generate();

        assertThat(consentRequest)
                .isEqualTo(
                        ConsentRequest.buildTypicalRecurring(
                                AccessEntity.builder()
                                        .availableAccountsWithBalance(AccessType.ALL_ACCOUNTS)
                                        .build(),
                                LOCAL_DATE_TIME_SOURCE));
    }

    private ManualAuthenticateRequest createRequest(Set<RefreshableItem> refreshableItems) {
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setRefreshableItemsIn(refreshableItems);
        ManualAuthenticateRequest request = new ManualAuthenticateRequest();
        request.setRefreshScope(refreshScope);
        return request;
    }

    private SparkassenConsentGenerator createGenerator(Set<RefreshableItem> refreshableItems) {
        return new SparkassenConsentGenerator(
                createRequest(refreshableItems),
                LOCAL_DATE_TIME_SOURCE,
                EnumSet.allOf(SparkassenScope.class));
    }
}
