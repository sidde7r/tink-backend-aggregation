package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreatePaymentRequest {

    @JsonIgnore private DnbPaymentType dnbPaymentType;

    @JsonProperty("creditorAccount")
    private AccountEntity creditor;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private String creditorName;

    private String creditorAgent;

    private String regulatoryReportingInformation;

    private String regulatoryReportingCode;

    private RemittanceInformationStructured remittanceInformationStructured;

    private String remittanceInformationUnstructured;

    private String frequency;

    private String startDate;

    private String endDate;
}
