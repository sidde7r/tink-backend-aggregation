package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@Setter
@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAccountEntity {

    private String iban;
    private String currency;
}
