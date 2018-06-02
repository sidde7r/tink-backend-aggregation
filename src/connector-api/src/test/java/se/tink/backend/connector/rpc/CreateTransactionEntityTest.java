package se.tink.backend.connector.rpc;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.TransactionTypes;

public class CreateTransactionEntityTest {

    private Validator validator;
    private CreateTransactionEntity entity;

    @Before
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        entity = new CreateTransactionEntity();
        entity.setType(TransactionTypes.DEFAULT);
        entity.setAmount(-50d);
        entity.setDescription("someDescription");
        entity.setExternalId("someExternalId");
        entity.setDate(new Date());
    }

    @Test
    public void isNonPendingTransactionValid() throws Exception {
        entity.setPending(false);

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void isPendingTransactionValid() throws Exception {
        entity.setPending(true);

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void isPendingTransactionValidWithExpirationDate() throws Exception {
        entity.setPending(true);
        entity.setPayload(ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, new Date()));

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void isNonPendingTransactionNotValidWithExpirationDate() throws Exception {
        entity.setPending(false);
        entity.setPayload(ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, new Date()));

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertFalse(violations.isEmpty());
    }

    @Test
    public void isNonPendingTransactionValidWithoutExpirationDate() throws Exception {
        entity.setPending(true);

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void isPendingTransactionNotValidWithWrongExpirationDateType() throws Exception {
        entity.setPending(true);
        entity.setPayload(ImmutableMap.of(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE, "[]"));

        Set<ConstraintViolation<CreateTransactionEntity>> violations = validator.validate(entity);
        for (ConstraintViolation<CreateTransactionEntity> violation : violations) {
            System.out.println(violation.getPropertyPath().toString() + ": " + violation.getMessage());
        }
        Assert.assertFalse(violations.isEmpty());
    }

}
