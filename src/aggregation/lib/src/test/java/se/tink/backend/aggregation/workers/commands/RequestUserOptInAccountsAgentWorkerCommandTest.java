package se.tink.backend.aggregation.workers.commands;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.strings.StringUtils;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RequestUserOptInAccountsAgentWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private ConfigureWhitelistInformationRequest request;
    private ControllerWrapper controllerWrapper;
    private RequestUserOptInAccountsAgentWorkerCommand command;

    @Before
    public void setup() {
        context = Mockito.mock(AgentWorkerCommandContext.class);
        request = Mockito.mock(ConfigureWhitelistInformationRequest.class);
        controllerWrapper = Mockito.mock(ControllerWrapper.class);
        command =
                new RequestUserOptInAccountsAgentWorkerCommand(context, request, controllerWrapper);
    }

    @Test
    public void returnContinueWhenContextAndRequestAccountsAreEmpty() throws Exception {
        Mockito.when(context.getCachedAccounts()).thenReturn(Lists.emptyList());
        Mockito.when(request.getAccounts()).thenReturn(Lists.emptyList());

        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, command.execute());
    }

    @Test
    public void ensureSupplementalInformationHavePortfolioTypesWhenAccountHaveIt()
            throws Exception {
        Account account = new Account();
        account.setBankId(StringUtils.generateUUID());
        account.setType(AccountTypes.INVESTMENT);

        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.PENSION);

        AccountFeatures features = AccountFeatures.createForPortfolios(portfolio);
        Credentials credentials = new Credentials();

        List<Account> accountsInRequest = Lists.newArrayList(account);
        List<Pair<Account, AccountFeatures>> accountsInContext =
                Lists.newArrayList(Pair.of(account, features));

        Mockito.when(request.getAccounts()).thenReturn(accountsInRequest);
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures()).thenReturn(accountsInContext);
        Mockito.when(context.requestSupplementalInformation(credentials)).thenReturn("{}");

        command.execute();

        Assert.assertTrue(
                credentials
                        .getSupplementalInformation()
                        .contains("\\\"portfolioTypes\\\":[\\\"PENSION\\\"]"));
    }

    @Test
    public void ensureSupplementalInformationDoNotHavePortfolioTypesWhenAccountDoNotHaveIt()
            throws Exception {
        Account account = new Account();
        account.setBankId(StringUtils.generateUUID());
        account.setType(AccountTypes.CHECKING);

        List<Account> accountsInRequest = Lists.newArrayList(account);
        List<Pair<Account, AccountFeatures>> accountsInContext =
                Lists.newArrayList(Pair.of(account, AccountFeatures.createEmpty()));
        Credentials credentials = new Credentials();

        Mockito.when(request.getAccounts()).thenReturn(accountsInRequest);
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures()).thenReturn(accountsInContext);
        Mockito.when(context.requestSupplementalInformation(credentials)).thenReturn("{}");

        command.execute();

        Assert.assertFalse(credentials.getSupplementalInformation().contains("portfolioTypes"));
    }
}
