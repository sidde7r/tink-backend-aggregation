package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.TransactionMapping.MERCHANT_COUNTER_PARTY_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.TransactionMapping.OUT_DIRECTION;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.TransactionMapping.PENDING_STATUS_LIST;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.TransactionMapping.RELEVANT_STATUS_LIST;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class TransactionEntity {
    private String feedItemUid;
    private String categoryUid;
    private AmountEntity amount;
    private AmountEntity sourceAmount;
    private String direction;
    private String source;
    private String sourceSubType;
    private String status;
    private String reference;
    private String counterPartyType;
    private String counterPartyName;
    private String country;
    private String userNote;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant updatedAt;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant transactionTime;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant settlementTime;

    public boolean isRelevant() {
        return RELEVANT_STATUS_LIST.contains(status.toLowerCase());
    }

    public boolean isPending() {
        return PENDING_STATUS_LIST.contains(status.toLowerCase());
    }

    public boolean isMerchantCounterPartyType() {
        return MERCHANT_COUNTER_PARTY_TYPE.equals(counterPartyType);
    }

    public boolean isOutDirection() {
        return OUT_DIRECTION.equals(direction);
    }

    public boolean hasSettlementTime() {
        return settlementTime != null;
    }

    public boolean hasFeedItemUid() {
        return feedItemUid != null;
    }

    public boolean hasReference() {
        return reference != null;
    }
}
