package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error;

import java.util.List;
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
    private List<String> key;
    private List<String> action;
}
