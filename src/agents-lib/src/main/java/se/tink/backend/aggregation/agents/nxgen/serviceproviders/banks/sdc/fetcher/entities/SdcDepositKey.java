package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

/*
"type": "ISK",
"custodyNumber": "100011"
 */
@JsonObject
public class SdcDepositKey {
    private String type;
    private String custodyNumber;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("custodyNumber", custodyNumber)
                .toString();
    }

    public String getType() {
        return type;
    }

    public String getCustodyNumber() {
        return custodyNumber;
    }
}
