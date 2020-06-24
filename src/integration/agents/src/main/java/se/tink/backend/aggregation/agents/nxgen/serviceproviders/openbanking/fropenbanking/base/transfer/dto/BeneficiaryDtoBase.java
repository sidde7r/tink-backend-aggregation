package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto;

public interface BeneficiaryDtoBase {

    CreditorAccountDtoBase getCreditorAccount();

    CreditorAgentDtoBase getCreditorAgent();

    CreditorDtoBase getCreditor();
}
