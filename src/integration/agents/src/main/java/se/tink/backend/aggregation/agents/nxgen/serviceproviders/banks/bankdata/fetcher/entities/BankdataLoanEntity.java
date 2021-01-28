package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BankdataLoanEntity extends BankdataAccountEntity {

    public LoanAccount toTinkLoan() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanDetails())
                .withId(getLoanId())
                .addHolderName(getAccountOwner())
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, getRegNo())
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, getAccountNo())
                .build();
    }

    private LoanModule getLoanDetails() {
        return LoanModule.builder()
                .withType(Type.DERIVE_FROM_NAME)
                .withBalance(ExactCurrencyAmount.of(getBalance(), getCurrencyCode()))
                .withInterestRate(0) // not supported by the app
                .setInitialBalance(ExactCurrencyAmount.of(getDrawingRight(), getCurrencyCode()))
                .setLoanNumber(getAccountNo())
                .build();
    }

    private IdModule getLoanId() {
        return IdModule.builder()
                .withUniqueIdentifier(getIban())
                .withAccountNumber(getAccountNo())
                .withAccountName(getName())
                .addIdentifier(new IbanIdentifier(getIban()))
                .setProductName(getName())
                .build();
    }
}
