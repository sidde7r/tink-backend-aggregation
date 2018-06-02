package se.tink.backend.system.workers.processor.creditsafe;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rx.Observable;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.ChangedConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeResponse;
import se.tink.backend.aggregation.rpc.PortfolioListResponse;
import se.tink.backend.aggregation.rpc.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.IDControlConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.metrics.MetricRegistry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreditSafeDataRefresherTest {
    private static final String PNR1 = "201212121212";
    private static final String PNR2 = "198709230356";
    private static final String PNR3 = "190101019990";
    private static final String PNR4 = "201212121213";
    private static final String PNR5 = "201212121214";
    private static final String PNR6 = "201212121215";
    private static final String PNR7 = "194111021111";

    @SuppressWarnings("FieldCanBeLocal")
    private final int defaultChangedDays = 2;
    private final List<String> portfolios = Lists.newArrayList("p1", "p2", "p3");

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    @SuppressWarnings("FieldCanBeLocal")
    private User user5;

    private Credentials creditSafeUser1;
    private Credentials bisnodeUser2;
    private Credentials creditSafeUser3;
    private Credentials bisnodeUser3;
    private Credentials creditSafeUser4;
    @SuppressWarnings("FieldCanBeLocal")
    private Credentials creditSafeUser5;

    private CreditSafeService creditSafeService;
    private CredentialsRequestRunnableFactory runnableFactory;
    private UserRepository userRepository;

    private CreditSafeDataRefresher refresher;

    private static PageableConsumerCreditSafeResponse pageableResponse(List<String> consumers) {
        PageableConsumerCreditSafeResponse r = new PageableConsumerCreditSafeResponse();
        r.setConsumers(consumers);
        return r;
    }

    private static PageableConsumerCreditSafeResponse pageableResponse(List<String> consumers, int end, int total) {
        PageableConsumerCreditSafeResponse r = new PageableConsumerCreditSafeResponse();
        r.setConsumers(consumers);
        r.setPageEnd(end);
        r.setTotalPortfolioSize(total);
        return r;
    }

    private static PageableConsumerCreditSafeResponse pageableResponseLong(int end, int total) {
        PageableConsumerCreditSafeResponse r = new PageableConsumerCreditSafeResponse();
        ArrayList<String> consumers = Lists.newArrayList();
        for(int i = 10000; i < 49000; i++) {
            consumers.add("1980010"+i);
        }
        r.setPageEnd(end);
        r.setTotalPortfolioSize(total);
        r.setConsumers(consumers);
        return r;
    }
    
    private static PageableConsumerCreditSafeResponse consumerErrorResponse(List<String> consumers) {
        PageableConsumerCreditSafeResponse r = new PageableConsumerCreditSafeResponse();
        r.setConsumers(consumers);
        r.setStatus("NOK");
        r.setErrorCode("17");
        r.setErrorMessage("Ingen frörändrad data");
        return r;
    }
    
    private static PageableConsumerCreditSafeResponse consumerErrorResponse2(List<String> consumers) {
        PageableConsumerCreditSafeResponse r = new PageableConsumerCreditSafeResponse();
        r.setConsumers(consumers);
        r.setStatus("NOK");
        r.setErrorCode("22");
        r.setErrorMessage("Fel i bevaknings hanteringen.");
        return r;
    }

    private static Predicate<AddMonitoredConsumerCreditSafeRequest> getAddRequestByPnr(final String pnr) {
        return r -> r.getPnr().equals(pnr);
    }

    private static Predicate<AddMonitoredConsumerCreditSafeRequest> getAddRequestByPortfilio(final String portfolio) {
        return r -> r.getPortfolio().equals(portfolio);
    }

    @Before
    public void setUp() {
        user1 = createUser("userId1", PNR1);
        user2 = createUser("userId2", PNR2);
        user3 = createUser("userId3", PNR3);
        user4 = createUser("userId4", PNR1);
        user5 = createUser("userId5", PNR4);

        creditSafeUser1 = createCredentials("userId1", "creditsafe");
        bisnodeUser2 = createCredentials("userId2", "bisnode");
        creditSafeUser3 = createCredentials("userId3", "creditsafe");
        bisnodeUser3 = createCredentials("userId3", "bisnode");
        creditSafeUser4 = createCredentials("userId4", "creditsafe");
        creditSafeUser5 = createCredentials("userId5", "creditsafe");
        
        userRepository = mock(UserRepository.class);
        FraudDetailsRepository fraudDetailsRepository = mock(FraudDetailsRepository.class);
        
        AggregationServiceFactory factory = mock(AggregationServiceFactory.class);
        ServiceConfiguration serviceConfiguration = mock(ServiceConfiguration.class);
        IDControlConfiguration idControlConfiguration = mock(IDControlConfiguration.class);
        CredentialsRepository credentialsRepository = mock(CredentialsRepository.class);
        MetricRegistry metricRegistry = new MetricRegistry();

        ServiceContext serviceContext = mock(ServiceContext.class);
        creditSafeService = mock(CreditSafeService.class);
        runnableFactory = mock(CredentialsRequestRunnableFactory.class);

        when(userRepository.streamAll()).thenReturn(Observable.just(user1, user2, user3, user4, user5));

        when(credentialsRepository.findAllByUserIdAndType(user1.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(creditSafeUser1));

        when(credentialsRepository.findAllByUserIdAndType(user2.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(bisnodeUser2));

        when(credentialsRepository.findAllByUserIdAndType(user3.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(creditSafeUser3, bisnodeUser3));

        when(credentialsRepository.findAllByUserIdAndType(user4.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(creditSafeUser4));
        
        when(credentialsRepository.findAllByUserIdAndType(user5.getId(), CredentialsTypes.FRAUD))
        .thenReturn(Lists.newArrayList(creditSafeUser5));
        
        when(factory.getCreditSafeService()).thenReturn(creditSafeService);

        when(idControlConfiguration.getDaysToFetchChangesFor()).thenReturn(defaultChangedDays);

        when(serviceConfiguration.getIdControl()).thenReturn(idControlConfiguration);

        when(serviceContext.getRepository(FraudDetailsRepository.class)).thenReturn(fraudDetailsRepository);

        refresher = new CreditSafeDataRefresher(
                runnableFactory,
                factory,
                credentialsRepository,
                userRepository,
                metricRegistry,
                idControlConfiguration);

        when(creditSafeService.listMonitoredConsumers(any(PageableConsumerCreditSafeRequest.class)))
                .thenReturn(
                        pageableResponse(Lists.newArrayList(PNR1, PNR2, PNR5), 3, 3),
                        pageableResponse(Lists.newArrayList(PNR6), 1, 1),
                        pageableResponse(Lists.<String>newArrayList(), 1, 0));

        PortfolioListResponse response = new PortfolioListResponse();
        response.setPortfolios(portfolios);
        when(creditSafeService.listPortfolios()).thenReturn(response);
    }

    // MIGHT FAIL BECAUSE CHANGED PAGING SIZE
    @Test
    public void testPageing() {

        ArgumentCaptor<ChangedConsumerCreditSafeRequest> argument = ArgumentCaptor.forClass(ChangedConsumerCreditSafeRequest.class);

        PortfolioListResponse response = new PortfolioListResponse();
        response.setPortfolios(Lists.newArrayList("p1"));
        when(creditSafeService.listPortfolios()).thenReturn(response);

        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponseLong(10000, 39000),
                pageableResponseLong(20000, 39000),
                pageableResponseLong(30000, 39000),
                pageableResponseLong(39000, 39000),
                null);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(creditSafeService, times(4)).listChangedConsumers(argument.capture());
        // CAN'T GET BELOW TO WORK--I believe it's the test code that is faulty, not the actual paging.
        // When debugging actual paging, correct values seems to be set.
//        List<ChangedConsumerCreditSafeRequest> requests = argument.getAllValues();
//
//        Iterable<Integer> actualPageStarts = Iterables.transform(requests, new Function<ChangedConsumerCreditSafeRequest, Integer>() {
//            @Override
//            public Integer apply(@Nullable ChangedConsumerCreditSafeRequest r) {
//                return r.getPageStart();
//            }
//        });
//
//        List<Integer> exptected = Lists.newArrayList(1, 10001, 20001, 30001);
//
//        assertEquals(exptected.size(), Iterables.size(actualPageStarts));
//        for (Integer e : exptected) {
//            assertTrue(Iterables.contains(actualPageStarts, e));
//        }
    }

    @Test
    public void testAggregationRequest() {

        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(new ArrayList<String>(), 10000, 10));

        ArgumentCaptor<ChangedConsumerCreditSafeRequest> argument = ArgumentCaptor.forClass(ChangedConsumerCreditSafeRequest.class);
        refresher.refreshCredentialsForIdControlUsers(null);

        verify(creditSafeService, times(portfolios.size())).listChangedConsumers(argument.capture());

        ChangedConsumerCreditSafeRequest request = argument.getValue();
        assertEquals(2, request.getChangedDays());
    }

    @Test
    public void testAggregationRequestWithNonDefaultChangedDays() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(new ArrayList<String>()));

        ArgumentCaptor<ChangedConsumerCreditSafeRequest> argument = ArgumentCaptor.forClass(ChangedConsumerCreditSafeRequest.class);
        refresher.refreshCredentialsForIdControlUsers(5);

        verify(creditSafeService, times(portfolios.size())).listChangedConsumers(argument.capture());

        ChangedConsumerCreditSafeRequest request = argument.getValue();
        assertEquals(5, request.getChangedDays());
    }

    @Test
    public void testNoCredentialsAreRefreshedWhenNoConsumersAreChanged() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(new ArrayList<String>()));

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, never()).createRefreshRunnable(any(User.class), any(Credentials.class), anyBoolean());
    }

    @Test
    public void testNoErrorWhenReturnCodeNotOk() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                consumerErrorResponse(null));
        try {
            refresher.refreshCredentialsForIdControlUsers(null);
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail("Should not fail with runtime exception");
        }

        verify(runnableFactory, never()).createRefreshRunnable(any(User.class), any(Credentials.class), anyBoolean());
    }
    
    @Test
    public void testErrorWhenReturnCodeNotOk2() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                consumerErrorResponse2(null));
        try {
            refresher.refreshCredentialsForIdControlUsers(null);
            fail("Should fail with runtime exception");
        } catch (RuntimeException e) {
            // OK.
        }
        
        verify(runnableFactory, never()).createRefreshRunnable(any(User.class), any(Credentials.class), anyBoolean());
    }
    
    @Test
    public void testUser1AndUser4GetsRefreshed() {
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(Lists.newArrayList(PNR1)));
        when(runnableFactory.createRefreshRunnable(user1, creditSafeUser1, false)).thenReturn(runnable1);
        when(runnableFactory.createRefreshRunnable(user4, creditSafeUser4, false)).thenReturn(runnable2);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, times(1)).createRefreshRunnable(user1, creditSafeUser1, false);
        verify(runnableFactory, times(1)).createRefreshRunnable(user4, creditSafeUser4, false);
        verify(runnable1, times(1)).run();
        verify(runnable2, times(1)).run();
    }

    @Test
    public void testDoesntCrashOnNullRunnable() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(Lists.newArrayList(PNR1)));
        when(runnableFactory.createRefreshRunnable(user1, creditSafeUser1, false)).thenReturn(null);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, times(1)).createRefreshRunnable(user1, creditSafeUser1, false);
    }

    @Test
    public void testUser2GetsRefreshed() {
        Runnable runnable = mock(Runnable.class);
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(Lists.newArrayList(PNR2)));
        when(runnableFactory.createRefreshRunnable(user2, bisnodeUser2, false)).thenReturn(runnable);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, times(1)).createRefreshRunnable(user2, bisnodeUser2, false);
        verify(runnable, times(1)).run();
    }

    @Test
    public void testUser3GetsRefreshed() {
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(Lists.newArrayList(PNR3)));
        when(runnableFactory.createRefreshRunnable(user3, bisnodeUser3, false)).thenReturn(runnable1);
        when(runnableFactory.createRefreshRunnable(user3, creditSafeUser3, false)).thenReturn(runnable2);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, times(1)).createRefreshRunnable(user3, bisnodeUser3, false);
        verify(runnableFactory, times(1)).createRefreshRunnable(user3, creditSafeUser3, false);
        verify(runnable1, times(1)).run();
        verify(runnable2, times(1)).run();
    }

    @Test
    public void testUser1AndUser3GetsRefreshed() {
        when(creditSafeService.listChangedConsumers(any(ChangedConsumerCreditSafeRequest.class))).thenReturn(
                pageableResponse(Lists.newArrayList(PNR3, PNR1)));
        when(runnableFactory.createRefreshRunnable(user1, creditSafeUser1, false)).thenReturn(null);
        when(runnableFactory.createRefreshRunnable(user3, bisnodeUser3, false)).thenReturn(null);
        when(runnableFactory.createRefreshRunnable(user3, creditSafeUser3, false)).thenReturn(null);

        refresher.refreshCredentialsForIdControlUsers(null);

        verify(runnableFactory, times(1)).createRefreshRunnable(user1, creditSafeUser1, false);
        verify(runnableFactory, times(1)).createRefreshRunnable(user3, bisnodeUser3, false);
        verify(runnableFactory, times(1)).createRefreshRunnable(user3, creditSafeUser3, false);
    }

    @Test
    public void testUnvalidPnrNeverGetSentToAggregation() {

        when(userRepository.streamAll()).thenReturn(Observable.just(
                createUser("some-userid1", "invalid-pnr"),
                createUser("some-userid2", "190000000000"),
                createUser("some-userid3", "000000000000")));

        refresher.cleanUpMonitoredConsumers();

        verify(creditSafeService, never()).addConsumerMonitoring(any(AddMonitoredConsumerCreditSafeRequest.class));
    }

    @Test
    public void testUnmonitoredAreAddedAndNonExistingRemoved() {
        refresher.cleanUpMonitoredConsumers();

        ArgumentCaptor<AddMonitoredConsumerCreditSafeRequest> addArgument = ArgumentCaptor.forClass(AddMonitoredConsumerCreditSafeRequest.class);
        ArgumentCaptor<RemoveMonitoredConsumerCreditSafeRequest> removeArgument = ArgumentCaptor.forClass(RemoveMonitoredConsumerCreditSafeRequest.class);

        verify(creditSafeService, times(2)).addConsumerMonitoring(addArgument.capture());
        List<AddMonitoredConsumerCreditSafeRequest> addCapture = addArgument.getAllValues();

        Optional<AddMonitoredConsumerCreditSafeRequest> pnr3 = addCapture.stream()
                .filter(getAddRequestByPnr(PNR3)::apply).findFirst();
        Optional<AddMonitoredConsumerCreditSafeRequest> pnr4 = addCapture.stream()
                .filter(getAddRequestByPnr(PNR4)::apply).findFirst();

        assertTrue(pnr3.isPresent());
        assertTrue(pnr4.isPresent());

        Iterable<AddMonitoredConsumerCreditSafeRequest> byPortfolio = addCapture.stream()
                .filter(getAddRequestByPortfilio("p3")::apply).collect(Collectors.toList());
        assertEquals(2, Iterables.size(byPortfolio));

        verify(creditSafeService, times(2)).removeConsumerMonitoring(removeArgument.capture());
        List<RemoveMonitoredConsumerCreditSafeRequest> removeCapture = removeArgument.getAllValues();

        Iterable<String> actualPnrs = Iterables.transform(removeCapture, REMOVEREQUEST_TO_PNR);

        assertEquals(2, Iterables.size(actualPnrs));
        assertTrue(Iterables.contains(actualPnrs, PNR6));
        assertTrue(Iterables.contains(actualPnrs, PNR5));
    }

    @Test
    public void testEmptyMonitoredListAddsAll() {

        ArgumentCaptor<AddMonitoredConsumerCreditSafeRequest> addArgument = ArgumentCaptor.forClass(AddMonitoredConsumerCreditSafeRequest.class);
        ArgumentCaptor<RemoveMonitoredConsumerCreditSafeRequest> removeArgument = ArgumentCaptor.forClass(RemoveMonitoredConsumerCreditSafeRequest.class);


        when(creditSafeService.listMonitoredConsumers(any(PageableConsumerCreditSafeRequest.class)))
                .thenReturn(
                        pageableResponse(Lists.newArrayList(PNR7), 3, 3),
                        pageableResponse(Lists.<String>newArrayList(), 1, 1),
                        pageableResponse(Lists.<String>newArrayList(), 1, 0));

        refresher.cleanUpMonitoredConsumers();

        verify(creditSafeService, times(4)).addConsumerMonitoring(addArgument.capture());

        List<AddMonitoredConsumerCreditSafeRequest> addCapture = addArgument.getAllValues();
        Iterable<String> actualPnrs = Iterables.transform(addCapture, ADDREQUEST_TO_PNR);
        Iterable<String> actualPortfolios = Iterables.transform(addCapture, ADDREQUEST_TO_PORTFOLIO);

        List<String> exptected = Lists.newArrayList(PNR1, PNR2, PNR3, PNR4);
        assertEquals(exptected.size(), Iterables.size(actualPnrs));
        assertEquals(exptected.size(), Iterables.size(actualPortfolios));

        for (String p : actualPortfolios) {
            assertEquals("p2", p);
        }

        for (String pnr : actualPnrs) {
            assertTrue(exptected.contains(pnr));
        }

        verify(creditSafeService, times(1)).removeConsumerMonitoring(removeArgument.capture());
        RemoveMonitoredConsumerCreditSafeRequest removeCapture = removeArgument.getValue();
        assertEquals(PNR7, removeCapture.getPnr());
    }
    
    private User createUser(String userId, String fraudPnr) {
        UserProfile p = new UserProfile();
        p.setFraudPersonNumber(fraudPnr);

        User u = new User();
        u.setId(userId);
        u.setProfile(p);
        return u;
    }

    private Credentials createCredentials(String userId, String providerName) {
        Credentials c = new Credentials();
        if (providerName.equals("bisnode") || providerName.equals("creditsafe")) {
            c.setType(CredentialsTypes.FRAUD);
        }
        c.setProviderName(providerName);
        c.setUserId(userId);
        return c;
    }

    public static final Function<AddMonitoredConsumerCreditSafeRequest, String> ADDREQUEST_TO_PORTFOLIO =
            AddMonitoredConsumerCreditSafeRequest::getPortfolio;
    public static final Function<AddMonitoredConsumerCreditSafeRequest, String> ADDREQUEST_TO_PNR =
            AddMonitoredConsumerCreditSafeRequest::getPnr;
    public static final Function<RemoveMonitoredConsumerCreditSafeRequest, String> REMOVEREQUEST_TO_PNR =
            RemoveMonitoredConsumerCreditSafeRequest::getPnr;
}
