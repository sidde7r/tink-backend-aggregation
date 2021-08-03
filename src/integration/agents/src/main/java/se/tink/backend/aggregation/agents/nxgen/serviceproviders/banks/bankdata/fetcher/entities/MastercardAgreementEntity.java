package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class MastercardAgreementEntity {

    private String agreementId;
    private String agreementName;
    private Double balance;
    private Double availableBalance;
    private Double maxBalance;
    private String regNo;
    private String accountNo;
    private Boolean canDeposit;
    private List<MastercardEntity> mastercardCards;
}
