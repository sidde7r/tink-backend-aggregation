package se.tink.backend.categorization.rules;

import java.util.Optional;
import org.junit.Test;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroIcsCategorizationCommandTest {

    @Test
    public void testMerchantWithoutMerchantCategoryDescription() {

        Transaction transaction = new Transaction();
        transaction.setInternalPayloadSerialized(null);

        Optional<CategorizationVector> categoryVector = AbnAmroIcsCategorizationCommand
                .getMerchantCategoryVector(transaction);

        assertThat(categoryVector.isPresent()).isFalse();
    }

    @Test
    public void testMerchantWithNotMappedMerchantCategory() {

        Transaction transaction = new Transaction();
        transaction.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.MERCHANT_DESCRIPTION, "foo-bar");

        Optional<CategorizationVector> categoryVector = AbnAmroIcsCategorizationCommand
                .getMerchantCategoryVector(transaction);

        assertThat(categoryVector.isPresent()).isFalse();
    }

    @Test
    public void testMerchantWithMappedMerchantCategory() {

        Transaction transaction = new Transaction();
        transaction.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.MERCHANT_DESCRIPTION, "CAR RENTAL AGENCIES");

        Optional<CategorizationVector> categoryVector = AbnAmroIcsCategorizationCommand
                .getMerchantCategoryVector(transaction);

        assertThat(categoryVector.isPresent()).isNotNull();
    }
}
