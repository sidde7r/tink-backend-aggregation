package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode(callSuper = false)
@JsonObject
@Data
public class AccountDetailsResponse extends AbstractResponse {
    private AccountInterestDetailsEntity accountInterestDetails;
    private List<String> accountOwners = Collections.emptyList();
    private BigDecimal feeAmount;
    private String feeCurrency;
    private String accountType;
    private String iban;
    private String bic;
}
