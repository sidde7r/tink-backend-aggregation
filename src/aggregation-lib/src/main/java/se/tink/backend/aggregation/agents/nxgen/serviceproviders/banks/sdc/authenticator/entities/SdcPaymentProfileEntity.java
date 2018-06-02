package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcPaymentProfileEntity {
    private String defaultPaymentAccount;
    private String paymentConfirmation;
    private Boolean paymentShowSortOrder;
    private Boolean paymentShowRegistration;
    private Boolean paymentDueDateDefault;
    private SdcAmount maxPaymentAmount;
}
