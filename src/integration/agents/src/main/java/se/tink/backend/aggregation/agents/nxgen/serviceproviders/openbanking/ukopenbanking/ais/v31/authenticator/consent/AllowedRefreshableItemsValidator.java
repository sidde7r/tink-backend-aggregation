package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.RefreshableItem;

/**
 * Auto refreshes are always trying to fetch all items, which would cause consent permissions
 * exceed. To solve this problem items expected to be refreshed are saved when full authentication
 * happens. Why? Because this the time when set of items is converted to specific permissions of the
 * consent.
 *
 * <p>Eventually this should be handled in the upper layer if possible (RefreshExecutorUtils?)
 */
@Slf4j
public class AllowedRefreshableItemsValidator {

    public static final String ITEMS_ALLOWED_TO_BE_REFRESHED = "allowed_items";

    private final PersistentStorage storage;

    public AllowedRefreshableItemsValidator(PersistentStorage storage) {
        this.storage = storage;
    }

    public void save(Set<RefreshableItem> itemsExpectedToBeRefreshed) {
        storage.put(ITEMS_ALLOWED_TO_BE_REFRESHED, itemsExpectedToBeRefreshed);
        log.info(
                "Saved items expected to be refreshed to be re-used during auto refresh: `{}`",
                itemsExpectedToBeRefreshed);
    }

    public boolean isForbiddenToBeRefreshed(RefreshableItem item) {
        Set<RefreshableItem> itemsAllowedToBeRefreshed = restore();

        // TODO: To be removed after 1.08.2021 (required for backward compatibility 90 days
        // period)
        if (itemsAllowedToBeRefreshed.isEmpty()) {
            return false;
        }

        return !itemsAllowedToBeRefreshed.contains(item);
    }

    private Set<RefreshableItem> restore() {
        return storage.get(
                        ITEMS_ALLOWED_TO_BE_REFRESHED, new TypeReference<Set<RefreshableItem>>() {})
                .orElseGet(HashSet::new);
    }
}
