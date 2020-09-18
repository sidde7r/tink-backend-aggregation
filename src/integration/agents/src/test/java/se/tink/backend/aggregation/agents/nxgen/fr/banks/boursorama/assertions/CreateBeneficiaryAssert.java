package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.assertions;

import java.util.Objects;
import org.assertj.core.api.AbstractAssert;
import org.junit.Ignore;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@Ignore
public class CreateBeneficiaryAssert
        extends AbstractAssert<CreateBeneficiaryAssert, CreateBeneficiary> {
    private CreateBeneficiaryAssert(CreateBeneficiary actual) {
        super(actual, CreateBeneficiaryAssert.class);
    }

    public static CreateBeneficiaryAssert assertThat(CreateBeneficiary actual) {
        return new CreateBeneficiaryAssert(actual);
    }

    public CreateBeneficiaryAssert hasStatus(CreateBeneficiaryStatus status) {
        isNotNull();
        if (!Objects.equals(actual.getStatus(), status)) {
            failWithMessage(
                    "Expected createBeneficiary's status to be <%s> but was <%s>",
                    status, actual.getStatus());
        }
        return this;
    }

    public CreateBeneficiaryAssert hasOwnerAccountNumber(String ownerAccountNumber) {
        isNotNull();
        if (!Objects.equals(actual.getOwnerAccountNumber(), ownerAccountNumber)) {
            failWithMessage(
                    "Expected createBeneficiary's ownerAccountNumber to be <%s> but was <%s>",
                    ownerAccountNumber, actual.getOwnerAccountNumber());
        }
        return this;
    }
}
