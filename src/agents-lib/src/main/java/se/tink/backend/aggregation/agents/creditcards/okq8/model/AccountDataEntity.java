package se.tink.backend.aggregation.agents.creditcards.okq8.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import java.text.ParseException;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDataEntity {
    private String account;
    private String available;
    @JsonProperty("card_name")
    private String cardName;
    private String limit;
    private String ocr;
    @JsonProperty("owner_name")
    private String ownerName;
    private String saldo;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOcr() {
        return ocr;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }

    public double getSaldoAsDouble() {
        if (Strings.isNullOrEmpty(saldo)) {
            throw new NullPointerException("Account saldo not present. It should be.");
        }

        return StringUtils.parseAmount(saldo);
    }

    public Account toTinkAccount() throws ParseException {
        Account account = new Account();

        double negativeSaldo = -getSaldoAsDouble();
        account.setBalance(negativeSaldo);

        account.setBankId(getAccount());
        account.setName("OKQ8 Visa");
        account.setType(AccountTypes.CREDIT_CARD);

        Preconditions.checkState(Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{11}"),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }
}
