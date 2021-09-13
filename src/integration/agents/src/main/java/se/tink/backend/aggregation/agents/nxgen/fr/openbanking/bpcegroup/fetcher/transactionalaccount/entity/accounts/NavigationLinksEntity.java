package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NavigationLinksEntity {

    private NavigationLinkEntity transactions;
    private NavigationLinkEntity balances;
}
