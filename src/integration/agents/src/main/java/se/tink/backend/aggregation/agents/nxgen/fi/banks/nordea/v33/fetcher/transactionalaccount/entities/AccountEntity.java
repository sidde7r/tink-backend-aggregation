package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty public PermissionsEntity permissions;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("display_account_number")
    private String accountId;

    @JsonProperty private String iban;
    @JsonProperty private String bic;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty private String nickname;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty private String category;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("booked_balance")
    private double bookedBalance;

    @JsonProperty("credit_limit")
    private double creditLimit;

    @JsonProperty("available_balance")
    private double availableBalance;

    @JsonProperty private String currency;
    @JsonProperty private List<AccountOwnerEntity> roles;

    public String getCategory() {
        return category;
    }

    public TransactionalAccount toTinkAccount() {

        return TransactionalAccount.builder(
                        NordeaFIConstants.ACCOUNT_TYPE_MAPPER.translate(category).get(),
                        iban,
                        new Amount(NordeaFIConstants.CURRENCY, availableBalance))
                .setHolderName(
                        roles.stream()
                                .filter(AccountOwnerEntity::isOwner)
                                .map(AccountOwnerEntity::getHolderName)
                                .findFirst()
                                .orElse(null))
                .setName(nickname)
                .setBalance(new Amount(NordeaFIConstants.CURRENCY, availableBalance))
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setAccountNumber(accountId)
                .setBankIdentifier(accountNumber)
                .build();
    }
}
