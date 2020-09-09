package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetPinResponseEntity {

    private String key;
    private String value;
}
