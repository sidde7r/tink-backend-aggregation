package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;

@JsonObject
public final class IngHolder {

    private IngInterventionDegree interventionDegree;
    private String name;
    private String nif;
    private String completeName;
    private String type;
    private String typeCode;

    public String getNif() {
        return nif;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getType() {
        return type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public IngInterventionDegree getInterventionDegree() {
        return interventionDegree;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getAnyName() {
        if (completeName != null) {
            return completeName;
        }
        return name;
    }

    @JsonIgnore
    public String getAnyTypeCode() {
        if (typeCode != null) {
            return typeCode;
        }
        return interventionDegree.getCode();
    }

    public static List<Holder> getHolders(IngProduct product) {
        return product.getHolders().stream().map(IngHolder::toHolder).collect(Collectors.toList());
    }

    public static Holder toHolder(IngHolder ingHolder) {
        return Holder.of(ingHolder.getAnyName(), toHolderRole(ingHolder.getAnyTypeCode()));
    }

    public static Holder.Role toHolderRole(String typeCode) {
        switch (typeCode) {
            case IngConstants.HolderTypes.OWNER:
            case IngConstants.HolderTypes.CO_OWNER:
                return Holder.Role.HOLDER;
            case IngConstants.HolderTypes.AUTHORIZED_USER:
                return Holder.Role.AUTHORIZED_USER;
            default:
                return Holder.Role.OTHER;
        }
    }
}
