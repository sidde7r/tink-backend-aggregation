package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
public class CardEntity {
    private String maskedCardNumberDebit;
    private String maskedCardNumber;
    private String expireYY;
    private String expireMM;
    private String createDate;
    private String cardType;
    private String cardStatus;
    private String cardLogo;
    @Getter private String cardId;
    private String cardGroup;
    private String cardCategory;
    private String blockStatus;
    private String accountNumber;
}
