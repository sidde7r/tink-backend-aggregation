package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AccountInfoEntity {
    private String number;
    private String type;
}
