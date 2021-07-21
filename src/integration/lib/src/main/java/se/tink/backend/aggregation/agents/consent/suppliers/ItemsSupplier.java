package se.tink.backend.aggregation.agents.consent.suppliers;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemsSupplier {

    public static Set<RefreshableItem> get(CredentialsRequest request) {
        if (isManualAuthenticationRequest(request)) {
            Set<RefreshableItem> items =
                    getRefreshScope((ManualAuthenticateRequest) request)
                            .map(RefreshScope::getRefreshableItemsIn)
                            .orElseGet(Collections::emptySet);

            if (!items.isEmpty()) {
                return items;
            }
            log.warn(
                    "[CONSENT GENERATOR] ManualAuthenticateRequest has null or empty RefreshScope. "
                            + "Providing default items with identity data item.");
        } else {
            log.warn(
                    "[CONSENT GENERATOR] {} detected. "
                            + "Providing default items with identity data item.",
                    request.getClass().getCanonicalName());
        }

        return getDefaultItemsWithIdentityDataItem();
    }

    private static boolean isManualAuthenticationRequest(CredentialsRequest request) {
        return request instanceof ManualAuthenticateRequest;
    }

    private static Optional<RefreshScope> getRefreshScope(ManualAuthenticateRequest request) {
        return Optional.ofNullable(request.getRefreshScope());
    }

    private static Set<RefreshableItem> getDefaultItemsWithIdentityDataItem() {
        Set<RefreshableItem> itemsExpectedToBeRefreshed =
                Sets.newHashSet(RefreshableItem.allRefreshableItemsAsArray());
        itemsExpectedToBeRefreshed.add(RefreshableItem.IDENTITY_DATA);
        return itemsExpectedToBeRefreshed;
    }
}
