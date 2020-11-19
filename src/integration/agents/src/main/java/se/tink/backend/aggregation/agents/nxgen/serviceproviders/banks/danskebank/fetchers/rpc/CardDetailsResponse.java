package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsResponse extends AbstractResponse {
    private String panIdDebit;
    private String panIdCredit;
    private String maskedCardNumberDebit;
    private String maskedCardNumber;
    private String expireYY;
    private String expireMM;
    private String customerName;
    private String createDate;
    private String cardType;
    private String cardStatus;
    private String cardLogo;
    private String cardCategory;
    private String blockStatus;
    private String actions;
    private String accountNumber;
}
