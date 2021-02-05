package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.uuid.UUIDUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestUserOptInAccountsAgentWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private ConfigureWhitelistInformationRequest request;
    private ControllerWrapper controllerWrapper;
    private RequestUserOptInAccountsAgentWorkerCommand command;
    private LoginAgentEventProducer eventProducer;

    @Before
    public void setup() {
        context = Mockito.mock(AgentWorkerCommandContext.class);
        request = Mockito.mock(ConfigureWhitelistInformationRequest.class);
        controllerWrapper = Mockito.mock(ControllerWrapper.class);
        eventProducer = Mockito.mock(LoginAgentEventProducer.class);

        command =
                new RequestUserOptInAccountsAgentWorkerCommand(
                        context, request, controllerWrapper, eventProducer);
    }

    @Test
    public void returnContinueWhenContextAndRequestAccountsAreEmpty() throws Exception {
        Mockito.when(context.getCachedAccountsWithFeatures()).thenReturn(Lists.emptyList());
        Mockito.when(request.getAccounts()).thenReturn(Lists.emptyList());

        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, command.execute());
    }

    @Test
    public void ensureSupplementalInformationHavePortfolioTypesWhenAccountHaveIt()
            throws Exception {
        // given
        Credentials credentials = new Credentials();
        Mockito.when(request.getAccounts())
                .thenReturn(getAccountsInRequest(AccountTypes.INVESTMENT, false));
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures())
                .thenReturn(getAccountsInContext(AccountTypes.INVESTMENT, false, true));
        Mockito.when(
                        context.waitForSupplementalInformation(
                                eq(credentials.getId()), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of("{}"));

        // when
        command.execute();

        // then
        Assert.assertTrue(
                credentials
                        .getSupplementalInformation()
                        .contains("\\\"portfolioTypes\\\":[\\\"PENSION\\\"]"));
    }

    @Test
    public void ensureSupplementalInformationDoNotHavePortfolioTypesWhenAccountDoNotHaveIt()
            throws Exception {
        // given
        Credentials credentials = new Credentials();
        Mockito.when(request.getAccounts())
                .thenReturn(getAccountsInRequest(AccountTypes.CHECKING, false));
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures())
                .thenReturn(getAccountsInContext(AccountTypes.CHECKING, false, false));
        Mockito.when(
                        context.waitForSupplementalInformation(
                                eq(credentials.getId()), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of("{}"));

        // when
        command.execute();

        // then
        Assert.assertFalse(credentials.getSupplementalInformation().contains("portfolioTypes"));
    }

    @Test
    public void ensureSupplementalInformationHaveCurrencyCode() throws Exception {
        // given
        Credentials credentials = new Credentials();
        Mockito.when(request.getAccounts())
                .thenReturn(getAccountsInRequest(AccountTypes.CHECKING, true));
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures())
                .thenReturn(getAccountsInContext(AccountTypes.CHECKING, true, false));
        Mockito.when(
                        context.waitForSupplementalInformation(
                                eq(credentials.getId()), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of("{}"));

        // when
        command.execute();

        // then
        Assert.assertTrue(
                credentials
                        .getSupplementalInformation()
                        .contains("\\\"currencyCode\\\":\\\"USD\\\""));
    }

    @Test
    public void ensureEventEmittedWhenOptinTimeouted() throws Exception {
        // given
        Credentials credentials = new Credentials();
        CredentialsRequest mockedRequest =
                Mockito.mock(CredentialsRequest.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(request.getAccounts()).thenReturn(Lists.emptyList());
        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.when(context.getCachedAccountsWithFeatures())
                .thenReturn(getAccountsInContext(AccountTypes.CHECKING, false, false));
        Mockito.when(
                        context.waitForSupplementalInformation(
                                eq(credentials.getId()), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.empty());
        Mockito.when(context.getRequest()).thenReturn(mockedRequest);

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.ABORT, result);

        Mockito.verify(eventProducer)
                .sendLoginCompletedEvent(
                        Mockito.any(),
                        eq(LoginResult.OPTIN_ERROR_TIMEOUT),
                        anyLong(),
                        eq(
                                AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                        .UserInteractionInformation
                                        .MULTIPLE_FACTOR_USER_INTERACTION));
    }

    private List<Pair<Account, AccountFeatures>> getAccountsInContext(
            AccountTypes accountTypes, boolean withCurrency, boolean withFeatures) {

        AccountFeatures features;
        if (withFeatures) {
            Portfolio portfolio = new Portfolio();
            portfolio.setType(Portfolio.Type.PENSION);
            features = AccountFeatures.createForPortfolios(portfolio);
        } else {
            features = AccountFeatures.createEmpty();
        }

        return Lists.newArrayList(Pair.of(getAccount(accountTypes, withCurrency), features));
    }

    private List<Account> getAccountsInRequest(AccountTypes accountTypes, boolean withCurrency) {
        return Lists.newArrayList(getAccount(accountTypes, withCurrency));
    }

    private Account getAccount(AccountTypes accountTypes, boolean withCurrency) {
        Account account = new Account();
        account.setBankId(UUIDUtils.generateUUID());
        account.setType(accountTypes);
        if (withCurrency) {
            account.setCurrencyCode("USD");
        }

        return account;
    }
}
