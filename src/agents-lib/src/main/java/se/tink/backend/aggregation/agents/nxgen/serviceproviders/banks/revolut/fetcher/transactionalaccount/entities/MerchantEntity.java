package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MerchantEntity {
  private String id;
  private String scheme;
  private String name;
  private String country;
  private String city;
  private String mcc;
  private String category;
  private String bgColor;
  private String logo;
}
