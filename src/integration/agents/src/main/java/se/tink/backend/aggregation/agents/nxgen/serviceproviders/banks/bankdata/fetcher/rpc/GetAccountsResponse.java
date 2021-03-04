package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.MastercardAgreementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetAccountsResponse {
    private List<BankdataAccountEntity> accounts;
    private List<MastercardAgreementEntity> mastercardAgreements;

    public List<TransactionalAccount> getTinkAccounts() {
        return accounts.stream()
                .filter(accountIsNotALoan())
                .map(BankdataAccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Predicate<BankdataAccountEntity> accountIsNotALoan() {
        return accountEntity -> AccountTypes.LOAN != accountEntity.getType();
    }

    public List<BankdataAccountEntity> getAccounts() {
        return accounts;
    }

    public List<MastercardAgreementEntity> getMastercardAgreements() {
        return mastercardAgreements;
    }
}
