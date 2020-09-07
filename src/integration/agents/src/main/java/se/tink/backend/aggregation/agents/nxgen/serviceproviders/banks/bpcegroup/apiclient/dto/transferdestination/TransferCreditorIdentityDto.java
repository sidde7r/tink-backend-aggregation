package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class TransferCreditorIdentityDto {

    private List<CreditorUseItemDto> creditorUse;

    private boolean holderIndicator;

    private String activationDate;

    private RibDto rib;

    private String label;

    private TypeDto creditorType;

    private String reference;

    private BeneficiaryBalanceDto balance;

    private String iban;

    private String bankLabel;

    private String bic;

    private String designationLabel;

    private String email;
}
