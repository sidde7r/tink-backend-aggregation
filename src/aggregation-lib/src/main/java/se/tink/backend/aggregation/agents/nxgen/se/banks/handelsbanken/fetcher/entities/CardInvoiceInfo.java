package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardInvoiceInfo {
    private HandelsbankenAmount credit;
    private HandelsbankenAmount spendable;
    private HandelsbankenAmount usedCredit;

    public Optional<HandelsbankenAmount> findSpendable() {
        return Optional.ofNullable(spendable);
    }

    public Optional<HandelsbankenAmount> findUsedCredit() {
        return Optional.ofNullable(usedCredit);
    }
}
