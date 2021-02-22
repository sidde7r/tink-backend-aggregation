package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.AUTHORIZED_USER;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@JsonObject
public class HolderEntity {
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

    private static Party.Role toPartyRole(String holderDescription) {
        switch (holderDescription) {
            case ImaginBankConstants.HolderTypes.OWNER:
                return Party.Role.HOLDER;
            case ImaginBankConstants.HolderTypes.AUTHORIZED_USER:
                return AUTHORIZED_USER;
            default:
                return Party.Role.OTHER;
        }
    }

    private static Party toParty(HolderEntity holder) {
        return new Party(holder.getFullHolderName(), toPartyRole(holder.getDescription()));
    }

    public static List<Party> toParties(ListHoldersResponse response) {
        return response.getHolders().stream()
                .map(HolderEntity::toParty)
                .collect(Collectors.toList());
    }
}
