package se.tink.backend.aggregation.agents.consent;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class ConsentGenerator {

    private final PermissionsMapper mapper;

    public ConsentGenerator(PermissionsMapper mapper) {
        this.mapper = mapper;
    }

    public Set<String> generate(CredentialsRequest request, Set<String> availablePermissions) {
        Set<RefreshableItem> items =
                new RefreshableItemsProvider().getItemsExpectedToBeRefreshed(request);

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
