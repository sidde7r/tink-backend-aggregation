package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities;

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

    public Optional<HandelsbankenAmount> findSpendable() {
        return Optional.ofNullable(spendable);
    }

    public Optional<HandelsbankenAmount> findUsedCredit() {
        return Optional.ofNullable(usedCredit);
    }
}
