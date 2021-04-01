package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.NordeaLoanParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class LoansEntity {
    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;
    private String repaymentStatus;
    private String nickname;
    private AmountEntity amount;
    private CreditEntity credit;
    private InterestEntity interest;
    private List<OwnersEntity> owners;

    public Optional<LoanAccount> toBasicTinkLoanAccount() {
        return Optional.of(
                LoanAccount.nxBuilder()
                        .withLoanDetails(getLoanModule())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                NordeaLoanParsingUtils.loanIdToUniqueIdentifier(
                                                        loanId))
                                        .withAccountNumber(loanFormattedId)
                                        .withAccountName(getAccountName())
                                        .addIdentifier(new SwedishIdentifier(loanId))
                                        .setProductName(group)
                                        .build())
                        .sourceInfo(createAccountSourceInfo())
                        .build());
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductCode(productCode)
                .bankAccountType(group)
                .build();
    }

    private LoanModule getLoanModule() {
        return LoanModule.builder()
                .withType(getTinkLoanType())
                .withBalance(amount.getTinkBalance())
                .withInterestRate(interest.getTinkInterestRate().doubleValue())
                .setAmortized(amount.getTinkAmortized())
                .setInitialBalance(amount.getTinkInitialBalance())
                .setLoanNumber(loanId)
                .build();
    }

    @JsonIgnore
    private LoanDetails.Type getTinkLoanType() {
        return NordeaBaseConstants.LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    private String getAccountName() {
        return Strings.isNullOrEmpty(nickname) ? loanFormattedId : nickname;
    }
}
