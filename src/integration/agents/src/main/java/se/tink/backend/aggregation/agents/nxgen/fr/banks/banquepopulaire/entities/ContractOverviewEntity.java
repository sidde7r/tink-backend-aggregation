package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ContractIdentifierEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
@JsonObject
public class ContractOverviewEntity {
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

    @JsonIgnore
    public boolean isUnknownContractType() {
        return getTinkAccountType() == AccountTypes.OTHER;

    }

    @JsonIgnore
    public boolean isNonAccountContract() {
        return BanquePopulaireConstants.Account.isNonAccountContract(contractType.getCode());
    }
    @JsonIgnore
    public boolean isUnhandledLoanType() {
        return BanquePopulaireConstants.Loan.toTinkLoanType(productId.getCode()) == LoanDetails.Type.OTHER;
    }
    @JsonIgnore
    public AccountTypes getTinkAccountType() {
        return BanquePopulaireConstants.Account.toTinkAccountType(contractType.getCode());
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public TypeEntity getProductId() {
        return productId;
    }

    public ClientEntity getClient() {
        return client;
    }

    public TransactionalAccount toTinkTransactionalAccount() {
        return TransactionalAccount.builder(getTinkAccountType(),
                externalReference,
                balance.toTinkAmount())
                .setName(productId.getLabel())
                .setAccountNumber(externalReference)
                .setHolderName(new HolderName(client.getDescriptionClient()))
                .setBankIdentifier(createTransactionalAccountBankIdentifier())
                .build();
    }

    public String createTransactionalAccountBankIdentifier() {
        return String.format("%s-%s-%s", contractId.getBankCode(), contractType.getCode(), contractId.getIdentifier());
    }

    public String createContractBankIdentifier() {
        return String.format("%s-%s", contractId.getBankCode(), contractId.getIdentifier());
    }
}
