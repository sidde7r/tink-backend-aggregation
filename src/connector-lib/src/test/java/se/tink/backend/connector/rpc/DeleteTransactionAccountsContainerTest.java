package se.tink.backend.connector.rpc;

import com.google.common.collect.Lists;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class DeleteTransactionAccountsContainerTest {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void nonRealTimeContainer_givesValidationError() {
        DeleteTransactionAccountsContainer container = new DeleteTransactionAccountsContainer();
        DeleteTransactionAccountEntity transactionAccount = new DeleteTransactionAccountEntity();
        DeleteTransactionEntity transactionEntity = new DeleteTransactionEntity();

        transactionAccount.setBalance(2000.0);
        transactionAccount.setExternalId("testExternalId1");
        transactionEntity.setExternalId("testExternalId2");
        transactionAccount.setTransactions(Lists.newArrayList(transactionEntity));
        container.setTransactionAccounts(Lists.newArrayList(transactionAccount));
        container.setType(TransactionContainerType.HISTORICAL);

        Set<ConstraintViolation<DeleteTransactionAccountsContainer>> violations = validator.validate(container);

        // HISTORICAL type is not valid.
        assertFalse(violations.isEmpty());

        container.setType(TransactionContainerType.BATCH);
        violations = validator.validate(container);

        // BATCH type is not valid.
        assertFalse(violations.isEmpty());

        container.setType(TransactionContainerType.REAL_TIME);
        violations = validator.validate(container);

        // REAL_TIME type is valid.
        assertTrue(violations.isEmpty());
    }
}
