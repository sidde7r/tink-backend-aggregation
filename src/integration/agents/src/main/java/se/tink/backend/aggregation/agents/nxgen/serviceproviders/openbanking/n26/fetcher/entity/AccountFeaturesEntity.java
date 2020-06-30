package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountFeaturesEntity {

    private Boolean supportsInformation;
    private Boolean supportsSendPayment;
    private Boolean supportsReceivePayment;
}
