package se.tink.backend.aggregation.agents.nxgen.demo.banks.password;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.nxgen.framework.validation.AisValidator;
import se.tink.backend.aggregation.nxgen.framework.validation.ValidationResult;
import se.tink.libraries.credentials.service.RefreshableItem;

public class PasswordDemoAgentTest {
    private static final String USERNAME = "tink";
    private static final String PASSWORD = "tink-1234";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("be", "be-test-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .doLogout(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @Test
    public void testCheckingAccountsOnlyRefresh() throws Exception {
        // Arrange
        final ValidationResult[] validationResult = new ValidationResult[1];
        String onlyCheckingAccountsRule = "Only checking accounts";
        AisValidator validator =
                AisValidator.builder()
                        .ruleAccount(
                                onlyCheckingAccountsRule,
                                account -> account.getType() == AccountTypes.CHECKING,
                                account ->
                                        String.format(
                                                "There is also account with type %s",
                                                account.getType().toString()))
                        .setExecutor(result -> validationResult[0] = result)
                        .build();

        // Act
        builder.setRefreshableItems(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS))
                .setValidator(validator)
                .build()
                .testRefresh();

        // Assert
        boolean validationPassed =
                validationResult[0].getSubResults().get(onlyCheckingAccountsRule).passed();
        Assert.assertTrue(validationPassed);
    }

    @Test
    public void testSavingsAccountsOnlyRefresh() throws Exception {
        // Arrange
        final ValidationResult[] validationResult = new ValidationResult[1];
        String onlySavingsAccountsRule = "Only savings accounts";
        AisValidator validator =
                AisValidator.builder()
                        .ruleAccount(
                                onlySavingsAccountsRule,
                                account -> account.getType() == AccountTypes.SAVINGS,
                                account ->
                                        String.format(
                                                "There is also account with type %s",
                                                account.getType().toString()))
                        .setExecutor(result -> validationResult[0] = result)
                        .build();

        // Act
        builder.setRefreshableItems(Sets.newHashSet(RefreshableItem.SAVING_ACCOUNTS))
                .setValidator(validator)
                .build()
                .testRefresh();

        // Assert
        boolean validationPassed =
                validationResult[0].getSubResults().get(onlySavingsAccountsRule).passed();
        Assert.assertTrue(validationPassed);
    }
}
