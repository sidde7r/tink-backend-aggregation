package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountResultEntity {

    private List<CurrentEntity> listCurrent;

    public List<TransactionalAccount> toTransactionalAccount(){
        return listCurrent.stream()
                .map(CurrentEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    public String getLocalContractType(){
        return listCurrent.get(0).getAccountEntity().getAccountNumber().getLocalContractType();
    }
    public String getLocalContractDetail(){
        return listCurrent.get(0).getAccountEntity().getAccountNumber().getLocalContractDetail();
    }

    public String getCompanyId(){
        return listCurrent.get(0).getAccountEntity().getSubProductEntity().getProductEntity().getCompanyId();
    }

}
