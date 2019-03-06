package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardInvoiceInfo {
    private HandelsbankenAmount credit;
    private HandelsbankenAmount spendable;
    private HandelsbankenAmount usedCredit;

    public HandelsbankenAmount getCredit() {
        return credit;
    }

    public HandelsbankenAmount getSpendable() {
        return spendable;
    }

    @JsonIgnore
    public boolean hasCreditLimitLargerThanZero() {
        return credit != null && credit.asDouble() != null && credit.asDouble() > 0;
    }

    @JsonIgnore
    public boolean hasSpendable() {
        return spendable != null && spendable.asDouble() != null;
    }

    @JsonIgnore
    public Optional<HandelsbankenAmount> findSpendable() {
        return Optional.ofNullable(spendable);
    }

    @JsonIgnore
    public Optional<HandelsbankenAmount> findUsedCredit() {
        return Optional.ofNullable(usedCredit);
    }
}
