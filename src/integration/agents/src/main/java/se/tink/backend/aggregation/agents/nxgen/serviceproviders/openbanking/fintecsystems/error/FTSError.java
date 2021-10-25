package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FTSError {
    private String[] key;
    private String[] action;

    @Override
    public String toString() {
        return "FTSError{"
                + "key="
                + Arrays.toString(key)
                + ", action="
                + Arrays.toString(action)
                + '}';
    }
}
