package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.assertions;

import java.util.Objects;
import org.assertj.core.api.AbstractAssert;
import org.junit.Ignore;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Beneficiary;

@Ignore
public class BeneficiaryAssert extends AbstractAssert<BeneficiaryAssert, Beneficiary> {
    private BeneficiaryAssert(Beneficiary actual) {
        super(actual, BeneficiaryAssert.class);
    }

    public static BeneficiaryAssert assertThat(Beneficiary actual) {
        return new BeneficiaryAssert(actual);
    }

    public BeneficiaryAssert hasAccountNumber(String accountNumber) {
        isNotNull();
        if (!Objects.equals(actual.getAccountNumber(), accountNumber)) {
            failWithMessage(
                    "Expected beneficiary's accountNumber to be <%s> but was <%s>",
                    accountNumber, actual.getAccountNumber());
        }
        return this;
    }

    public BeneficiaryAssert hasAccountNumberType(AccountIdentifierType type) {
        isNotNull();
        if (!Objects.equals(actual.getAccountNumberType(), type)) {
            failWithMessage(
                    "Expected beneficiary's type to be <%s> but was <%s>",
                    type, actual.getAccountNumberType());
        }
        return this;
    }

    public BeneficiaryAssert hasName(String name) {
        isNotNull();
        if (!Objects.equals(actual.getName(), name)) {
            failWithMessage(
                    "Expected beneficiary's name to be <%s> but was <%s>", name, actual.getName());
        }
        return this;
    }
}
