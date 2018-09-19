package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ContractIdentifierEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class AccountOverviewEntity {
    @JsonProperty("idContrat")
    private IdentifierEntity contractId;
    @JsonProperty("referenceExterneContrat")
    private String externalReference;
    @JsonProperty("typeContrat")
    private TypeEntity contractType;
    @JsonProperty("idProduit")
    private TypeEntity productId;
    private List<ContractIdentifierEntity> idsFctContrat;
    private String codeFamilleProduit;
    private String intitulePersonnalise;
    @JsonProperty("soldeEncours")
    private AmountEntity balance;
    private ClientEntity client;

    public boolean isUnknownType() {
        if (contractType == null || contractType.getCode() == null) {
            return true;
        }

        return !BanquePopulaireConstants.AccountType.isHandled(contractType.getCode());
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(BanquePopulaireConstants.AccountType
                        .fromContractTypeCode(contractType.getCode()).getTinkType(),
                externalReference,
                balance.toTinkAmount())
                .setName(productId.getLabel())
                .setAccountNumber(externalReference)
                .setHolderName(new HolderName(client.getDescriptionClient()))
                .setBankIdentifier(createBankIdentifier())
                .build();
    }

    private String createBankIdentifier() {
        return String.format("%s-%s-%s", contractId.getBankCode(), contractType.getCode(), contractId.getIdentifier());
    }
}
