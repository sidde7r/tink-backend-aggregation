package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BrandEntity {
    private String name;
    private String logo;
    private List<BankEntity> banks;

    @JsonIgnore
    public Map<String, BankEntity> getBankConfiguration() {
        return banks.stream()
                .collect(Collectors.toMap(BankEntity::getId, Function.identity()));
    }
}
