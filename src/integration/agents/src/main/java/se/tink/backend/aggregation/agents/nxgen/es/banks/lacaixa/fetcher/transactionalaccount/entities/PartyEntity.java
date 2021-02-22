package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@JsonObject
public class PartyEntity {
    @JsonProperty("nombre")
    private String name;

    @JsonProperty("apellido1")
    private String firstSurname;

    @JsonProperty("apellido2")
    private String secondSurname;

    @JsonProperty("descripcion")
    private String description;

    private String getFullHolderName() {
        return String.format("%s %s %s", name, firstSurname, secondSurname);
    }

    private String getDescription() {
        return description;
    }

    private static Party.Role toHolderRole(String holderDescription) {
        switch (holderDescription) {
            case LaCaixaConstants.HolderTypes.OWNER:
                return Party.Role.HOLDER;
            case LaCaixaConstants.HolderTypes.AUTHORIZED_USER:
                return Party.Role.AUTHORIZED_USER;
            default:
                return Party.Role.OTHER;
        }
    }

    private static Party toParty(PartyEntity holder) {
        return new Party(holder.getFullHolderName(), toHolderRole(holder.getDescription()));
    }

    public static List<Party> toTinkParties(ListHoldersResponse response) {
        return response.getHolders().stream()
                .map(PartyEntity::toParty)
                .collect(Collectors.toList());
    }
}
