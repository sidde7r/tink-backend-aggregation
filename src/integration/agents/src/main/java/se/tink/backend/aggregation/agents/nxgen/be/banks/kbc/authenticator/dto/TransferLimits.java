package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValueEncodedTriplet;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferLimits {
    private TypeValueEncodedTriplet currency;
    private List<TypeValueEncodedTriplet> possibleLimits;
    private TypeValueEncodedTriplet defaultLimit;

    public TypeValueEncodedTriplet getCurrency() {
        return currency;
    }

    public List<TypeValueEncodedTriplet> getPossibleLimits() {
        return possibleLimits;
    }

    public TypeValueEncodedTriplet getDefaultLimit() {
        return defaultLimit;
    }
}
