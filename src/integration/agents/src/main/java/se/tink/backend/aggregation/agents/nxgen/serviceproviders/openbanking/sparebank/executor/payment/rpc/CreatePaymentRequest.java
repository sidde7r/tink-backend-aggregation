package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.RemittanceInformationStructured;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentRequest {
    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private String creditorName;
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private CreditorAddressEntity creditorAddress;
    private RemittanceInformationStructured remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;
    @JsonIgnore private SparebankPaymentType sparebankPaymentType;
}
