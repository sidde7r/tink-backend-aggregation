package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountBalanceEntity {
    private double availableBalance;
    private double accountingBalance;
    private double valueDatedBalance;
    private double creditLine;
    private double blockedAmount;
}
