package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.AllowedRefreshableItemsValidator.ITEMS_ALLOWED_TO_BE_REFRESHED;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AllowedRefreshableItemsValidatorTest {

    private AllowedRefreshableItemsValidator validator;

    private PersistentStorage storage;

    @Before
    public void setUp() throws Exception {
        this.storage = new PersistentStorage();
        this.validator = new AllowedRefreshableItemsValidator(storage);
    }

    @Test
    public void shouldSaveItemsSuccessfully() {
        // given
        Set<RefreshableItem> items =
                new HashSet<>(
                        Arrays.asList(
                                RefreshableItem.CHECKING_ACCOUNTS,
                                RefreshableItem.SAVING_ACCOUNTS));

        // when
        validator.save(items);

        // then
        Optional<Set<RefreshableItem>> restoredItems =
                storage.get(
                        ITEMS_ALLOWED_TO_BE_REFRESHED,
                        new TypeReference<Set<RefreshableItem>>() {});
        assertThat(restoredItems).isPresent();
        assertThat(restoredItems.get()).isNotEmpty();
        assertThat(restoredItems.get())
                .containsExactlyInAnyOrder(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS);
    }

    @Test
    public void testIsForbiddenToBeRefreshed() {
        // given
        Set<RefreshableItem> items =
                new HashSet<>(
                        Arrays.asList(
                                RefreshableItem.CHECKING_ACCOUNTS,
                                RefreshableItem.SAVING_ACCOUNTS));
        validator.save(items);

        // expected
        assertThat(validator.isForbiddenToBeRefreshed(RefreshableItem.IDENTITY_DATA)).isTrue();
        assertThat(validator.isForbiddenToBeRefreshed(RefreshableItem.CHECKING_ACCOUNTS)).isFalse();
        assertThat(validator.isForbiddenToBeRefreshed(RefreshableItem.SAVING_ACCOUNTS)).isFalse();
    }
}
