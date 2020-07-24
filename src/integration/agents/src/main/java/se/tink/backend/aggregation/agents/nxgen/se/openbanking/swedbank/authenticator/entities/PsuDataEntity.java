package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {
    private String bankID = RequestValues.SWEDBANK_BANKID;
}
