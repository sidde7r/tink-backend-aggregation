package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {

    private String id;
    private String created;
    private String description;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("sort_code")
    private String sortCode;

    private String type;

    @JsonIgnore private BalanceResponse balance;

    public void setBalance(BalanceResponse balance) {
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isTransactionalAccount() {
        return ACCOUNT_TYPE_MAPPER.isOneOf(getType(), TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public TransactionalAccount toTinkAccount() {
        final TransactionalAccount.Builder<?, ?> result =
                TransactionalAccount.builder(
                                ACCOUNT_TYPE_MAPPER.translate(getType()).get(),
                                accountNumber,
                                balance.getTotalBalance())
                        .setAccountNumber(accountNumber)
                        .setBankIdentifier(this.getId())
                        .setName(description);

        if (!Strings.isNullOrEmpty(sortCode) && !Strings.isNullOrEmpty(accountNumber)) {
            result.addIdentifier(
                    AccountIdentifier.create(
                            AccountIdentifier.Type.SORT_CODE, sortCode + accountNumber));
        }

        return result.build();
    }
}
