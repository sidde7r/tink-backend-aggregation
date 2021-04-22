package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@Getter
@JsonObject
public class PartyEntity {
    private String name;
    private String tradRelTir;

    private static Party.Role toHolderRole(String holderDescription) {
        switch (holderDescription) {
            case WizinkConstants.HolderTypes.OWNER:
                return Party.Role.HOLDER;
            case WizinkConstants.HolderTypes.AUTHORIZED_USER:
                return Party.Role.AUTHORIZED_USER;
            default:
                return Party.Role.OTHER;
        }
    }

    private static Party toParty(PartyEntity partyEntity) {
        return new Party(partyEntity.getName(), toHolderRole(partyEntity.getTradRelTir()));
    }

    public static List<Party> toTinkParties(List<PartyEntity> holders) {
        return holders.stream().map(PartyEntity::toParty).collect(Collectors.toList());
    }
}
