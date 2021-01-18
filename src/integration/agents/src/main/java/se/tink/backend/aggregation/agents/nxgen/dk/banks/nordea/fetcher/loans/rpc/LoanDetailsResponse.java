package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.PathValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.FinancedObjectEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.InterestRateConverter;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoanDetailsResponse {

    // In banks' response, interest rate is percentage value with four decimal places
    private static final int SCALE = 6;

    private static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "mortgage")
                    .put(LoanDetails.Type.OTHER, "other")
                    .build();

    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;

    private InterestEntity interest;
    private AmountEntity amount;
    private List<OwnersEntity> owners;

    private String nickname;

    private FinancedObjectEntity financedObject;

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanId)
                                .withAccountNumber(loanFormattedId)
                                .withAccountName(
                                        Optional.ofNullable(nickname).orElse(loanFormattedId))
                                .addIdentifier(new DanishIdentifier(loanId))
                                .setProductName(productCode)
                                .build())
                .setApiIdentifier(PathValues.ACCOUNT_ID_PREFIX + loanId)
                .putInTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE, productCode)
                .build();
    }

    private LoanModule getLoanModule() {
        List<String> applicants = getApplicants();
        return LoanModule.builder()
                .withType(getTinkLoanType())
                .withBalance(getLoanBalance())
                .withInterestRate(InterestRateConverter.toDecimalValue(interest.getRate(), SCALE))
                .setAmortized(getAmountPaid())
                .setInitialBalance(getInitialBalance())
                .setApplicants(applicants)
                .setCoApplicant(applicants.size() > 1)
                .setLoanNumber(loanId)
                .setNextDayOfTermsChange(interest.getInterestChangeDateAsLocalDate())
                .setSecurity(getLoanSecurity())
                .build();
    }

    private LoanDetails.Type getTinkLoanType() {
        return LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    private ExactCurrencyAmount getLoanBalance() {
        return new ExactCurrencyAmount(amount.getBalance(), currency);
    }

    private ExactCurrencyAmount getAmountPaid() {
        return Optional.ofNullable(amount.getPaid())
                .map(paid -> new ExactCurrencyAmount(paid, currency))
                .orElse(null);
    }

    private ExactCurrencyAmount getInitialBalance() {
        return Optional.ofNullable(amount.getGranted())
                .map(granted -> new ExactCurrencyAmount(granted, currency))
                .orElse(null);
    }

    private List<String> getApplicants() {
        return Optional.ofNullable(owners)
                .map(
                        ownersList ->
                                ownersList.stream()
                                        .map(OwnersEntity::getName)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private String getLoanSecurity() {
        return Optional.ofNullable(financedObject).map(FinancedObjectEntity::getName).orElse(null);
    }
}
