package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestTransactionDataEntity {

    private Date fromDate;
    private Date toDate;
    private String companyId;
    private String localContractType;
    private String localContractDetail;

    public RequestTransactionDataEntity(Date fromDate, Date toDate, String companyId, String localContractType,
            String localContractDetail) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.companyId = companyId;
        this.localContractType = localContractType;
        this.localContractDetail = localContractDetail;
    }

    public String toJson() {
        return String.format(SantanderConstants.JSON.TRANSACTION_SEARCH, fromDate.toInstant().toString(),
                toDate.toInstant().toString(), companyId, localContractType, localContractDetail);
    }
}
