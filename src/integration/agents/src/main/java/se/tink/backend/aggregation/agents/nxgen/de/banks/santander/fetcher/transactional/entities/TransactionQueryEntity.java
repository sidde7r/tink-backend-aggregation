package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionQueryEntity {

    @JsonProperty("getMoreBookedTransactionList_LA")
    private GetMoreBookedTransactionListLAEntity getMoreBookedTransactionListLAEntity;

    public TransactionQueryEntity(
            Date fromDate,
            Date toDate,
            String localContractType,
            String localContractDetail,
            String companyId) {
        getMoreBookedTransactionListLAEntity =
                new GetMoreBookedTransactionListLAEntity(
                        fromDate, toDate, localContractType, localContractDetail, companyId);
    }
}
