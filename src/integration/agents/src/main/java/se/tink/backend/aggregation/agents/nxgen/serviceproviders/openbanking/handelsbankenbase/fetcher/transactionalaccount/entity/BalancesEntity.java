package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {
  private List<BalanceEntity> balances;

  public List<BalanceEntity> getBalances() {
    return balances;
  }
}
