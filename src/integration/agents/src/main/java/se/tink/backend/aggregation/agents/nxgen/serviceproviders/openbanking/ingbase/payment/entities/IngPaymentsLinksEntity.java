package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class IngPaymentsLinksEntity {

    private String scaRedirect;
}
