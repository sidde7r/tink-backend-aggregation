package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferResponse extends HeaderResponse {
    private TypeValuePair remainingAmountDayLimit;
    private TypeValuePair currencyRemainingAmount;
    private TypeValuePair orderReferenceNo;
    private TypeValuePair newBalance;
    private TypeValuePair hasRemainingInterventions;
    private TypeValuePair crossReference;
}
