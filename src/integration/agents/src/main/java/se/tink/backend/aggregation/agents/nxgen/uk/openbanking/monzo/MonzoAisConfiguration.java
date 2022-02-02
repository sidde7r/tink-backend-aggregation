package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class MonzoAisConfiguration extends UkOpenBankingAisConfiguration {

  public MonzoAisConfiguration(Builder builder) {
    super(builder);
  }

  @Override
  public boolean isFetchingTransactionsFromTheNewestToTheOldest() {
    return false;
  }
}
