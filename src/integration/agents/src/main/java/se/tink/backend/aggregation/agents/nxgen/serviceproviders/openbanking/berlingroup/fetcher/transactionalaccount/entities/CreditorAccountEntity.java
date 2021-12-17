package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAccountEntity {
    private String bban;
    private String currency;
    @Getter private String iban;
    private String maskedPan;
    private String msisdn;
    private String pan;
}
