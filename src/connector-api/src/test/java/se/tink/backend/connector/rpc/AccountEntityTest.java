package se.tink.backend.connector.rpc;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.AccountTypes;

public class AccountEntityTest {

    @Test
    public void calculateBalanceRequest_requiresZeroBalance() {
        AccountEntity entity = new AccountEntity();

        entity.setExternalId("testExternalId");
        entity.setBalance(100.0);
        entity.setName("testName");
        entity.setType(AccountTypes.CHECKING);
        entity.setNumber("12345");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        // First make sure it validates.
        Set<ConstraintViolation<AccountEntity>> constraintViolations = validator.validate(entity);
        Assert.assertTrue(constraintViolations.isEmpty());

        // It's not OK to request balance calculation for new accounts. Make sure it doesn't validate now.
        Map<String, Object> payload = Maps.newHashMap();
        payload.put(PartnerAccountPayload.CALCULATE_BALANCE, true);
        entity.setPayload(payload);
        constraintViolations = validator.validate(entity);

        Assert.assertFalse(constraintViolations.isEmpty());

        // Make sure zero balance solves the problem.
        entity.setBalance(0.0);
        constraintViolations = validator.validate(entity);
        Assert.assertTrue(constraintViolations.isEmpty());
    }
}
