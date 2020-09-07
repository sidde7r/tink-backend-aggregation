package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.TypeDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AccountDto {

    @JsonProperty("idContrat")
    private ContractIdDto contractId;

    @JsonProperty("referenceExterneContrat")
    private String externalReference;

    @JsonProperty("typeContrat")
    private TypeDto contractType;

    @JsonProperty("idProduit")
    private TypeDto productId;

    private List<ContractTypeIdDto> idsFctContrat;

    private String codeFamilleProduit;

    private String intitulePersonnalise;

    @JsonProperty("soldeEncours")
    private BalanceDto balance;

    private ClientDto client;

    private boolean isRibable;
}
