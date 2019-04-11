package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsData {

    @JsonProperty("groupesRoles")
    private List<AccountGroupEntity> groups;

    @JsonProperty("sommeTotaleEpargne")
    private AmountEntity totalSavingsAmount;

    public List<AccountGroupEntity> getGroups() {
        return groups;
    }

    public Stream<AccountEntity> getBenefits() {
        return groups.stream().flatMap(g -> g.getBenefits().stream());
    }
}
