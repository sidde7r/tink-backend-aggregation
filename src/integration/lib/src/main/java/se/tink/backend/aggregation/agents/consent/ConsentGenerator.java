package se.tink.backend.aggregation.agents.consent;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ConsentGenerator {

    private final PermissionsMapper mapper;

    public ConsentGenerator(PermissionsMapper mapper) {
        this.mapper = mapper;
    }

    public ImmutableSet<String> generate(
            CredentialsRequest request, Set<String> availablePermissions) {

        return new RefreshableItemsProvider()
                .getItemsExpectedToBeRefreshed(request).stream()
                        .map(mapper::getPermissions)
                        .flatMap(Collection::stream)
                        .map(Permission::getValue)
                        .filter(availablePermissions::contains)
                        .collect(ImmutableSet.toImmutableSet());
    }
}
