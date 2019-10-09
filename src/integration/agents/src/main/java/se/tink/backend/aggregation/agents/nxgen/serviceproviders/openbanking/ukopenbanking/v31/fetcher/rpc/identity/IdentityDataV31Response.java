package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.identity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataV31Response extends BaseV31Response<List<IdentityDataEntity>> {
    public IdentityDataEntity toTinkIdentityData() {

        return getEntity().orElseGet(null);
    }

    private Optional<IdentityDataEntity> getEntity() {
        return getData().orElse(Collections.emptyList()).stream()
                .filter(e -> e.getName() != null)
                .findAny();
    }
}
