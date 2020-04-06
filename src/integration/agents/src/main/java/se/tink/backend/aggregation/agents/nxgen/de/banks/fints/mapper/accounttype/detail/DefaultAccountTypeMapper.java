package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail;

import java.util.Optional;
import org.apache.commons.lang3.Range;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

public class DefaultAccountTypeMapper implements AccountTypeMapper {

    // Categorization source:
    // https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
    // Page 122
    private static final Range<Integer> CHECKING_ACCOUNT_RANGE = Range.between(1, 9);
    private static final Range<Integer> SAVINGS_ACCOUNT_RANGE = Range.between(10, 19);
    private static final Range<Integer> TIME_DEPOSIT_ACCOUNT_RANGE = Range.between(20, 29);
    private static final Range<Integer> SECURITES_ACCOUNT_RANGE = Range.between(30, 39);
    private static final Range<Integer> LOAN_ACCOUNT_RANGE = Range.between(40, 49);
    private static final Range<Integer> CREDIT_CARD_RANGE = Range.between(50, 59);
    private static final Range<Integer> FUND_DEPOSIT_ACCOUNT_RANGE = Range.between(60, 69);
    private static final Range<Integer> BAUSPAR_ACCOUNT_RANGE = Range.between(70, 79);
    private static final Range<Integer> INSURANCE_CONTRACT_RANGE = Range.between(80, 89);
    private static final Range<Integer> OTHER_NOT_ASSIGNABLE_RANGE = Range.between(90, 99);

    @Override
    public Optional<AccountTypes> getAccountTypeFor(HIUPD basicAccountInformation) {
        return Optional.ofNullable(basicAccountInformation.getAccountType())
                .map(this::getAccountTypeFor);
    }

    private AccountTypes getAccountTypeFor(int finTsAccountType) {
        if (SAVINGS_ACCOUNT_RANGE.contains(finTsAccountType)) {
            return AccountTypes.SAVINGS;
        }
        if (CHECKING_ACCOUNT_RANGE.contains(finTsAccountType)) {
            return AccountTypes.CHECKING;
        }
        if (CREDIT_CARD_RANGE.contains(finTsAccountType)) {
            return AccountTypes.CREDIT_CARD;
        }
        if (SECURITES_ACCOUNT_RANGE.contains(finTsAccountType)
                || FUND_DEPOSIT_ACCOUNT_RANGE.contains(finTsAccountType)) {
            return AccountTypes.INVESTMENT;
        }
        return null;
    }
}
