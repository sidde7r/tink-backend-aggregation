package se.tink.backend.aggregation.agents.consent;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public abstract class ConsentGenerator {

    private final Set<RefreshableItem> items;
    private final Set<String> availablePermissions;
    private final PermissionsMapper mapper;

    public ConsentGenerator(
            CredentialsRequest request,
            Set<String> availablePermissions,
            PermissionsMapper mapper) {
        this.items = new RefreshableItemsProvider().getItemsExpectedToBeRefreshed(request);
        this.availablePermissions = availablePermissions;
        this.mapper = mapper;
    }

    public Set<String> generate() {
        Set<String> permissions =
                items.stream()
                        .map(mapper::getPermissions)
                        .flatMap(Collection::stream)
                        .map(Permission::getValue)
                        .filter(availablePermissions::contains)
                        .collect(
                                ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
        log.info(
                "[CONSENT GENERATOR] itemsToRefresh: {}, availablePermissions: {} -> generated permissions {}",
                items,
                availablePermissions,
                permissions);
        return permissions;
    }
}
