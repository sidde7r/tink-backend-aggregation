package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity.ProductListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountResponse {

    @JsonProperty("products")
    private ProductListEntity productListEntity;

    @JsonIgnore
    Logger logger = LoggerFactory.getLogger(AccountResponse.class);

    private AccountTypes getType(){
        String accountType = productListEntity.get(0).getType().toUpperCase();
        switch(accountType){
        case ErsteBankConstants.ACCOUNTYPE.CHECKING:
            return AccountTypes.CHECKING;
        default:
            logger.warn("{} {}", ErsteBankConstants.LOGTAG.UNKNOWN_ACCOUNT_TYPE, accountType);
            return AccountTypes.CHECKING;
        }
    }

    public String getId(){
        return productListEntity.get(0).getId();
    }

    public String getAccountNumber(){return productListEntity.get(0).getIdentifier();}

    private Amount getTinkBalance(){
        return productListEntity.get(0).getAmountEntity().getTinkBalance();
    }

    private String getIban(){
        return productListEntity.get(0).getAccountInfoEntity().getBankConnectionEntity().getIban();
    }

    private String getName(){
        return productListEntity.get(0).getDescription();
    }

    private HolderName getHolderName(){
        return new HolderName(productListEntity.get(0).getTitle());
    }


    public List<TransactionalAccount> toTransactionalAccount(){

        TransactionalAccount account = TransactionalAccount.builder(getType(), getId(), getTinkBalance())
                .setAccountNumber(getAccountNumber())
                .setName(getName())
                .setHolderName(getHolderName())
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban()))
                .build();

        return Arrays.asList(account);
    }

}
