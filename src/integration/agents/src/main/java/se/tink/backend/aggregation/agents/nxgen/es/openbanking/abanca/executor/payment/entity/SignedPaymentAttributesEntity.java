package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class SignedPaymentAttributesEntity {

    private AmountEntity amount;
    private String concept;
    private AccountInfoEntity originAccount;
    private AccountInfoEntity remoteAccount;
    private String recipientName;
    private Date executionDate;
    private SettlementEntity settlement;
    private String expensesCode;
}
