package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;

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

    private static Role toHolderRole(String holderDescription) {
        switch (holderDescription) {
            case LaCaixaConstants.HolderTypes.OWNER:
                return Holder.Role.HOLDER;
            case LaCaixaConstants.HolderTypes.AUTHORIZED_USER:
                return Holder.Role.AUTHORIZED_USER;
            default:
                return Holder.Role.OTHER;
        }
    }

    private static Holder toHolder(HolderEntity holder) {
        return Holder.of(holder.getFullHolderName(), toHolderRole(holder.getDescription()));
    }

    public static List<Holder> toTinkHolders(ListHoldersResponse response) {
        return response.getHolders().stream()
                .map(HolderEntity::toHolder)
                .collect(Collectors.toList());
    }
}
