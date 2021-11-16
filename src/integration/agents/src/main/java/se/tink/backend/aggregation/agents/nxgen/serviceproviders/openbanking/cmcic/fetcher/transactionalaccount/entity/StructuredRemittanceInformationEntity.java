package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class StructuredRemittanceInformationEntity {
    @JsonProperty("referredDocumentInformation")
    private ReferredDocumentInformationsEntity referredDocumentInformation = null;

    @JsonProperty("referredDocumentAmount")
    private RemittanceAmountEntity referredDocumentAmount = null;

    @JsonProperty("creditorReferenceInformation")
    private CreditorReferenceInformationEntity creditorReferenceInformation = null;

    @JsonProperty("invoicer")
    private PartyIdentificationEntity invoicer = null;

    @JsonProperty("invoicee")
    private PartyIdentificationEntity invoicee = null;

    @JsonProperty("taxRemittance")
    private TaxInformationEntity taxRemittance = null;

    @JsonProperty("garnishmentRemittance")
    private GarnishmentEntity garnishmentRemittance = null;
}
