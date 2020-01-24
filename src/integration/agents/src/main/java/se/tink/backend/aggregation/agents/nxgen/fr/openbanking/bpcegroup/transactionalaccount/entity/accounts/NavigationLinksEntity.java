package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NavigationLinksEntity {

    private NavigationLinkEntity transactions;
    private NavigationLinkEntity balances;
}
