package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class ReportBaseData {
    private String holder;
    private String iban;
    private String bic;
    private String bankCode;
    private String bankName;
    private String countryId;
    private String date;
    private String jointAccount;
    private String accountType;
}
