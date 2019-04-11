package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicingFundsAccountDetailsRequest {
    private String description;
    private String mobileWarning;
    private AmountEntity amount;
    private String owner;
    private String product;
    private String isOwner;
    private String iban;
    private String isIberSecurities;
    private String availability;
    private String alias;
    private String number;
    private String contractNumberFormatted;
    private String bic;
    private String isSBPManaged;
    private String joint;
    private int numOwners;

    @JsonIgnore
    public static ServicingFundsAccountDetailsRequest createRequestFromAccount(
            AccountEntity account) {
        ServicingFundsAccountDetailsRequest request = new ServicingFundsAccountDetailsRequest();

        request.description = account.getDescription();
        request.mobileWarning = account.getMobileWarning();
        request.amount =
                new AmountEntity(account.getAmount().getValue(), account.getAmount().getCurrency());
        request.owner = account.getOwner();
        request.product = account.getProduct();
        request.isOwner = String.valueOf(account.isOwner());
        request.iban = account.getIban();
        request.isIberSecurities = String.valueOf(account.isIberSecurities());
        request.availability = account.getAvailability();
        request.alias = account.getAlias();
        request.number = account.getNumber();
        request.contractNumberFormatted = account.getContractNumberFormatted();
        request.bic = account.getBic();
        request.isSBPManaged = String.valueOf(account.isSBPManaged());
        request.joint = account.getJoint();
        request.numOwners = account.getNumOwners();

        return request;
    }
}
