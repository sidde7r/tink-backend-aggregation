package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
public class CardEntity {
    private String maskedCardNumberDebit;
    @Getter private String maskedCardNumber;
    private String expireYY;
    private String expireMM;
    private String createDate;
    @Getter private String cardType;
    @Getter private String cardStatus;
    private String cardLogo;
    @Getter private String cardId;
    private String cardGroup;
    private String cardCategory;
    private String blockStatus;
    @Getter private String accountNumber;
}
