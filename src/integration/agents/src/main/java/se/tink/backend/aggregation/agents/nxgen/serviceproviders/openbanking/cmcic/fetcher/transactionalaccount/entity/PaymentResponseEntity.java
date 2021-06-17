package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseEntity {

    private String resourceId;
    private BeneficiaryEntity beneficiary;
    private PaymentInformationStatusCodeEntity paymentInformationStatusCode;
    private StatusReasonInformationEntity statusReasonInformation;
    private AccountIdentificationEntity debtorAccount;
}
