package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.SavingsSpaceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.UserFeatures;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SavingsSpaceResponse {
    private double totalBalance;
    private double visibleBalance;

    @JsonProperty("spaces")
    private List<SavingsSpaceEntity> spaces;

    private UserFeatures userFeatures;

    private boolean isEmpty() {
        return spaces == null || spaces.isEmpty();
    }

    public List<TransactionalAccount> toSavingsAccounts() {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        return spaces.stream()
                .filter(space -> !space.isPrimary())
                .map(SavingsSpaceEntity::toSavingsAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
