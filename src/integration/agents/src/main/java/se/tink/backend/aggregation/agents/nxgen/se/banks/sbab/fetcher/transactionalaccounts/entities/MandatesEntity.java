package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@JsonObject
@Getter
@Slf4j
public class MandatesEntity {
    private String displayName;
    private String mandateType;

    public Party toParty() {
        return new Party(displayName, toHolderRole());
    }

    private Party.Role toHolderRole() {
        switch (mandateType) {
            case SBABConstants.HolderTypes.OWNER:
            case SBABConstants.HolderTypes.CO_ACCOUNT_HOLDER:
                return Party.Role.HOLDER;
            default:
                log.warn("Unknown mandateType: {}. Role set to UNKNOWN", mandateType);
                return Party.Role.UNKNOWN;
        }
    }
}
