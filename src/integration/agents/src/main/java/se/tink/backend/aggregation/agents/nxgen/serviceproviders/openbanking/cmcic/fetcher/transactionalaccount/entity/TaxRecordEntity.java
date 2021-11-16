package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxRecordEntity {
    @JsonProperty("type")
    private String type = null;

    @JsonProperty("category")
    private String category = null;

    @JsonProperty("categoryDetails")
    private String categoryDetails = null;

    @JsonProperty("debtorStatus")
    private String debtorStatus = null;

    @JsonProperty("certificateIdentification")
    private String certificateIdentification = null;

    @JsonProperty("formsCode")
    private String formsCode = null;

    @JsonProperty("period")
    private TaxPeriodEntity period = null;

    @JsonProperty("taxAmount")
    private TaxAmountEntity taxAmount = null;

    @JsonProperty("additionalInformation")
    private String additionalInformation = null;
}
