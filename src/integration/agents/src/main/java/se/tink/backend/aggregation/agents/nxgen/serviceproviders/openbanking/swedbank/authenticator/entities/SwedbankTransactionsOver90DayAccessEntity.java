package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.IbanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
public class SwedbankTransactionsOver90DayAccessEntity {

    private List<IbanEntity> transactionsOver90Days = new ArrayList<>();

    @JsonIgnore
    public SwedbankTransactionsOver90DayAccessEntity addIbans(List<String> ibans) {
        this.transactionsOver90Days =
                ibans.stream().map(IbanEntity::new).collect(Collectors.toList());
        return this;
    }
}
