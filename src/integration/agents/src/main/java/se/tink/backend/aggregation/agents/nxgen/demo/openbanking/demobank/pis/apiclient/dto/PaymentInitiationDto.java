package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PaymentInitiationDto {

    private AccountIdentifierDto debtorAccount;

    private AccountIdentifierDto creditorAccount;

    private AmountDto amount;

    private String creditorName;

    private String remittanceInformationUnstructured;

    private RemittanceInformationStructuredDto remittanceInformationStructured;

    private String initiationDate;
}
