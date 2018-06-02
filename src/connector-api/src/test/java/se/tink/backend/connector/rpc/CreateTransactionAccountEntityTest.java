package se.tink.backend.connector.rpc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;
import se.tink.backend.core.TransactionTypes;
import static org.junit.Assert.assertTrue;

public class CreateTransactionAccountEntityTest {

    @Test
    public void nullBalance_isOkay_IfCalculateBalanceIsRequested() {
        CreateTransactionAccountEntity entity = new CreateTransactionAccountEntity();

        entity.setTransactions(Lists.newArrayList(createTestTransactionEntity()));
        entity.setExternalId("testExternalId");

        // It's OK for balance to be null if we're requested the calculation of the balance.
        Map<String, Object> payload = Maps.newHashMap();
        payload.put(PartnerAccountPayload.CALCULATE_BALANCE, true);
        entity.setPayload(payload);
        entity.setBalance(null);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CreateTransactionAccountEntity>> constraintViolations = validator
                .validate(entity);

        assertTrue(constraintViolations.isEmpty());
    }

    private CreateTransactionEntity createTestTransactionEntity() {
        CreateTransactionEntity entity = new CreateTransactionEntity();
        entity.setAmount(-1000.0);
        entity.setDate(new Date());
        entity.setExternalId("testExternalId");
        entity.setDescription("H&M");
        entity.setType(TransactionTypes.CREDIT_CARD);
        return entity;
    }
}
