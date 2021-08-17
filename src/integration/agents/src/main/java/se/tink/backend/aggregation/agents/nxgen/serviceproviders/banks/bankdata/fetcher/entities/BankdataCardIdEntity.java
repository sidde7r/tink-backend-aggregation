package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataCardIdEntity {

    private String cardNumber;
    private String maskedCardNumber;
}
