package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
  private Long httpCode;
  private String httpMessage;
  private String moreInformation;
}
