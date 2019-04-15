package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        return isEmpty()
                ? Collections.emptyList()
                : spaces.stream()
                        .filter(space -> !space.isPrimary())
                        .map(SavingsSpaceEntity::toSavingsAccount)
                        .collect(Collectors.toList());
    }
}
