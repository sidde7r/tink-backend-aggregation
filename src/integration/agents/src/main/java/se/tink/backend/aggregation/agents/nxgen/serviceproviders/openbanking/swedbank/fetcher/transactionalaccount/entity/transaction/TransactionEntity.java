package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    protected TransactionAmountEntity transactionAmount;
    protected String remittanceInformationUnstructured;
    protected String remittanceInformationStructured;
}
