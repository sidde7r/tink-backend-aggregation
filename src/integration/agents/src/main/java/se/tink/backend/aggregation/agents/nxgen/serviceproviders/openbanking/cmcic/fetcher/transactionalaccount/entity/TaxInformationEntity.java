package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxInformationEntity {
    @JsonProperty("creditor")
    private TaxPartyEntity creditor = null;

    @JsonProperty("debtor")
    private TaxPartyEntity debtor = null;

    @JsonProperty("ultimateDebtor")
    private TaxPartyEntity ultimateDebtor = null;

    @JsonProperty("administrationZone")
    private String administrationZone = null;

    @JsonProperty("referenceNumber")
    private String referenceNumber = null;

    @JsonProperty("method")
    private String method = null;

    @JsonProperty("totalTaxableBaseAmount")
    private AmountTypeEntity totalTaxableBaseAmount = null;

    @JsonProperty("totalTaxAmount")
    private AmountTypeEntity totalTaxAmount = null;

    @JsonProperty("date")
    private LocalDate date = null;

    @JsonProperty("sequenceNumber")
    private BigDecimal sequenceNumber = null;

    @JsonProperty("record")
    private List<TaxRecordEntity> record = null;
}
