package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextPaymentEntity {
    private DateEntity dueDate;
    private AmountEntity expensesAmount;
    private AmountEntity interestAmount;
    private AmountEntity repaymentAmount;
    private AmountEntity totalAmount;
}
