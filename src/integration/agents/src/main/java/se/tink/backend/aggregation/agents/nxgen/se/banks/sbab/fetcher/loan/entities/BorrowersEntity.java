package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BorrowersEntity {
    private String displayName;
    private BigDecimal participationShare;

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getParticipationShare() {
        return participationShare;
    }
}
