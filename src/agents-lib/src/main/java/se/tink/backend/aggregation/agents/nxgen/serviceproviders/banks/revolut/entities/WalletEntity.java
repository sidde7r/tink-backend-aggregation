package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WalletEntity {
  private String id;
  private String ref;
  private String state;
  private String baseCurrency;
  private int topupLimit;
  private int totalTopup;
  private List<PocketEntity> pockets;

  public String getId() {
    return id;
  }

  public String getRef() {
    return ref;
  }

  public String getState() {
    return state;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public int getTopupLimit() {
    return topupLimit;
  }

  public int getTotalTopup() {
    return totalTopup;
  }

  public List<PocketEntity> getPockets() {
    return pockets;
  }
}
