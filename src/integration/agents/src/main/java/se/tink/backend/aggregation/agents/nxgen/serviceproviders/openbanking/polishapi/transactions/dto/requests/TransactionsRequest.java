package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.requests.RequestHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@SuperBuilder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionsRequest {
    RequestHeaderEntity requestHeader;

    String accountNumber;
    String transactionDateFrom;
    String transactionDateTo;
    String pageId;
    Integer perPage;
}
