package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.rpc;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.HomesEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
@NonNull
public class MortgageDTO {
    private MortgageDetailsResponse mortgageDetailsResponse;
    private HomesEntity homesEntity;
    private String mortgageId;

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(Type.MORTGAGE)
                                .withBalance(
                                        ExactCurrencyAmount.inDKK(
                                                getAmount(
                                                        mortgageDetailsResponse
                                                                .getLoanStatus()
                                                                .getOutstandingDebt())))
                                .withInterestRate(parseInterest())
                                .setInitialBalance(
                                        ExactCurrencyAmount.inDKK(
                                                getAmount(
                                                        mortgageDetailsResponse
                                                                .getMoreDetails()
                                                                .getPrincipalAmount())))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(mortgageId)
                                .withAccountNumber(homesEntity.getPropertyNo())
                                .withAccountName(homesEntity.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.DK,
                                                homesEntity.getPropertyNo(),
                                                homesEntity.getName()))
                                .build())
                .build();
    }

    private double getAmount(String amount) {
        return AgentParsingUtils.parseAmount(amount);
    }

    private double parseInterest() {
        final String interestString =
                mortgageDetailsResponse.getMoreDetails().getInterestRate().replaceAll(" ", "");
        return AgentParsingUtils.parsePercentageFormInterest(interestString);
    }
}
