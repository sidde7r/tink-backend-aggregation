package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OpBankCreditEntity {
    private String agreementNumber;
    private String agreementNumberIban;
    private String encryptedAgreementNumber;
    private String name;
    private double balance;
    private String grantedAmount;
    private String withdrawableAmount;
    private String usageCode;
    private String usage;
    private String creditType;
    private OpBankIssuerEntity issuer;
    private List<OpBankPartiesEntity> parties;
    private double calculatedWithdrawnAmount;
    private double paidAmount;
    private String personalizedName;

    @JsonIgnore
    public String getLoanName() {
        if (Strings.isNullOrEmpty(personalizedName)) {
            return name;
        }

        return personalizedName;
    }

    @JsonIgnore
    private LoanDetails.Type getLoanType(OpBankCreditEntity creditEntity) {
        if (!OpBankConstants.Fetcher.COLLATERAL_CREDIT.equalsIgnoreCase(creditEntity.getCreditType())) {
            return LoanDetails.Type.OTHER;
        }

        return OpBankConstants.LoanType.findLoanType(usage).getTinkType();
    }

    // this credit account is continuing credit
    @JsonIgnore
    public CreditCardAccount toTinkCreditAccount() {
        return CreditCardAccount.builder(agreementNumberIban,
                Amount.inEUR(balance),
                Amount.inEUR(AgentParsingUtils.parseAmount(withdrawableAmount)))
                .setAccountNumber(agreementNumberIban)
                .setBankIdentifier(encryptedAgreementNumber)  // to fetch transactions
                .setName(getCreditAccountName())
                .build();
    }

    @JsonIgnore
    private String getCreditAccountName() {
        if (Strings.isNullOrEmpty(personalizedName)) {
            return name;
        }

        return personalizedName;
    }

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public String getAgreementNumberIban() {
        return agreementNumberIban;
    }

    public String getEncryptedAgreementNumber() {
        return encryptedAgreementNumber;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public String getGrantedAmount() {
        return grantedAmount;
    }

    public String getWithdrawableAmount() {
        return withdrawableAmount;
    }

    public String getUsageCode() {
        return usageCode;
    }

    public String getUsage() {
        return usage;
    }

    public String getCreditType() {
        return creditType;
    }

    public OpBankIssuerEntity getIssuer() {
        return issuer;
    }

    public List<OpBankPartiesEntity> getParties() {
        return parties;
    }

    public double getCalculatedWithdrawnAmount() {
        return calculatedWithdrawnAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public String getPersonalizedName() {
        return personalizedName;
    }
}
