package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.SimpleAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@Getter
@JsonObject
public class CreatePaymentRequest {

    private String endToEndIdentification;

    private SimpleAccountEntity debtorAccount;

    private AmountEntity instructedAmount;

    private SimpleAccountEntity creditorAccount;

    private String creditorAgent;

    private String creditorName;

    private String chargeBearer;

    private String remittanceInformationUnstructured;

    private String serviceLevelCode;

    private String localInstrumentCode;

    private String requestedExecutionDate;
}
