package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FTSError {
    String[] key;
    String[] action;
}
