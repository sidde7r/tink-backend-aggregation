package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataCardEntity {

    private BankdataCardIdEntity cardKeyJson;
    private BankdataAccountIdEntity account;
    private String cardName;
    private String imageEnum;
    private String cardStatus;
    private String expirationDate;
}
