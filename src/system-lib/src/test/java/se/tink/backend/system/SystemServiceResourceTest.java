package se.tink.backend.system;

import com.google.common.collect.Lists;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableExecutor;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.SubscriptionToken;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.system.resources.CronServiceResource;
import se.tink.libraries.date.DateUtils;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemServiceResourceTest {

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;

    private ServiceContext context;
    private MailSender mailSender;
    private ServiceConfiguration configuration;
    private CategoryRepository categoryRepository;
    private CredentialsRepository credentialsRepository;
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private SubscriptionTokenRepository subscriptionTokenRepository;
    private ProviderRepository providerRepository;

    private static List<Category> createCategories(CategoryConfiguration categoryConfiguration) {
        Category uncat = new Category();
        uncat.setCode(categoryConfiguration.getExpenseUnknownCode());

        Category exclude = new Category();
        exclude.setCode(categoryConfiguration.getExcludeCode());

        return Lists.newArrayList(uncat, exclude);
    }

    private void createUsers() {
        UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");

        user1 = new User();
        user1.setId("11111111111111111111111111111111");
        user1.setUsername("user1@email.com");
        user1.setCreated(DateUtils.addDays(new Date(), -63));
        user1.setProfile(profile);

        user2 = new User();
        user2.setId("22222222222222222222222222222222");
        user2.setUsername("user2@email.com");
        user2.setCreated(DateUtils.addDays(new Date(), -63));
        user2.setProfile(profile);

        user3 = new User();
        user3.setId("33333333333333333333333333333333");
        user3.setUsername("user3@email.com");
        user3.setCreated(DateUtils.addDays(new Date(), -63));
        user3.setProfile(profile);

        user4 = new User();
        user4.setId("44444444444444444444444444444444");
        user4.setUsername("user4@email.com");
        user4.setCreated(DateUtils.addDays(new Date(), -63));
        user4.setProfile(profile);

        user5 = new User();
        user5.setId("55555555555555555555555555555555");
        user5.setUsername("user5@email.com");
        user5.setCreated(DateUtils.addDays(new Date(), -46));
        user5.setProfile(profile);

        UserState us1 = new UserState();
        us1.setUserId(user1.getId());
        us1.setHaveHadTransactions(false);

        UserState us2 = new UserState();
        us2.setUserId(user2.getId());
        us2.setHaveHadTransactions(true);

        UserState us3 = new UserState();
        us3.setUserId(user3.getId());
        us3.setHaveHadTransactions(false);

        UserState us4 = new UserState();
        us4.setUserId(user4.getId());
        us4.setHaveHadTransactions(false);

        UserState us5 = new UserState();
        us4.setUserId(user5.getId());
        us4.setHaveHadTransactions(false);

        Credentials c3 = new Credentials();
        c3.setUserId(user3.getId());
        c3.setType(CredentialsTypes.FRAUD);
        c3.setStatus(CredentialsStatus.UPDATED);

        Credentials c4 = new Credentials();
        c4.setUserId(user4.getId());
        c4.setType(CredentialsTypes.FRAUD);
        c4.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);

        when(userRepository.streamAll()).thenReturn(Observable.just(user1, user2, user3, user4, user5));

        when(userStateRepository.findOneByUserId(user1.getId())).thenReturn(us1);
        when(userStateRepository.findOneByUserId(user2.getId())).thenReturn(us2);
        when(userStateRepository.findOneByUserId(user3.getId())).thenReturn(us3);
        when(userStateRepository.findOneByUserId(user4.getId())).thenReturn(us4);
        when(userStateRepository.findOneByUserId(user5.getId())).thenReturn(us5);

        when(credentialsRepository.findAllByUserIdAndType(user3.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(c3));
        when(credentialsRepository.findAllByUserIdAndType(user4.getId(), CredentialsTypes.FRAUD))
                .thenReturn(Lists.newArrayList(c4));
    }

    @Before
    public void setUp() {
        mailSender = mock(MailSender.class);
        context = mock(ServiceContext.class);
        configuration = mock(ServiceConfiguration.class);

        userRepository = mock(UserRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        userStateRepository = mock(UserStateRepository.class);
        credentialsRepository = mock(CredentialsRepository.class);
        providerRepository = mock(ProviderRepository.class);
        subscriptionTokenRepository = mock(SubscriptionTokenRepository.class);

        CategoryConfiguration categoryConfiguration = new SECategories();

        when(categoryRepository.findLeafCategories()).thenReturn(createCategories(categoryConfiguration));
        when(categoryRepository.findAll()).thenReturn(createCategories(categoryConfiguration));
        when(categoryRepository.findByCode(categoryConfiguration.getSalaryCode())).thenReturn(new Category());
        when(categoryRepository.findByCode(categoryConfiguration.getExpenseUnknownCode())).thenReturn(new Category());
        when(credentialsRepository.findStatusDistribution()).thenReturn(
                new HashMap<String, Map<CredentialsStatus, BigInteger>>());
        when(subscriptionTokenRepository.save(any(SubscriptionToken.class))).thenReturn(new SubscriptionToken());
        when(providerRepository.findAll()).thenReturn(Collections.<Provider>emptyList());

        // Ripped from `development.yml`.
        NotificationsConfiguration notificationConfig = new NotificationsConfiguration();
        notificationConfig.setEnabled(false);

        when(configuration.getNotifications()).thenReturn(notificationConfig);

        when(context.getCategoryConfiguration()).thenReturn(categoryConfiguration);

        when(context.getMailSender()).thenReturn(mailSender);

        // Mocked metric registry (`mock(MetricRegistry.class)`) causes timers to become `null`, which causes exceptions.
        MetricRegistry metricRegistry = new MetricRegistry();

        when(context.getConfiguration()).thenReturn(configuration);
        when(context.getTrackingExecutorService()).thenReturn(
                (ListenableThreadPoolExecutor<Runnable>) mock(
                        ListenableThreadPoolExecutor.class));

        when(context.getRepository(UserRepository.class)).thenReturn(userRepository);
        when(context.getRepository(CategoryRepository.class)).thenReturn(categoryRepository);
        when(context.getRepository(UserStateRepository.class)).thenReturn(userStateRepository);
        when(context.getRepository(CredentialsRepository.class)).thenReturn(credentialsRepository);
        when(context.getRepository(SubscriptionTokenRepository.class)).thenReturn(subscriptionTokenRepository);
        when(context.getRepository(ProviderRepository.class)).thenReturn(providerRepository);
        when(context.getRepository(MarketRepository.class)).thenReturn(mock(MarketRepository.class));
        when(context.getRepository(CurrencyRepository.class)).thenReturn(mock(CurrencyRepository.class));
        when(context.getRepository(SubscriptionRepository.class)).thenReturn(mock(SubscriptionRepository.class));
        when(context.getRepository(UserFacebookProfileRepository.class))
                .thenReturn(mock(UserFacebookProfileRepository.class));
        when(context.getRepository(UserFacebookFriendRepository.class))
                .thenReturn(mock(UserFacebookFriendRepository.class));

        createUsers();
    }

    @Test
    public void testSoftReferenceNullability() {
        SoftReference<Integer> a = new SoftReference<>(null);mock(MailSender.class);
        Assert.assertNull(a.get());
    }

    @Test
    public void testSendUserActivationReminder() {
        CronServiceResource
                .sendUserActivationReminder(userRepository, credentialsRepository,
                        userStateRepository, mailSender, mock(AnalyticsController.class));

        // Verify only 2 calls in total
        verify(mailSender, times(2)).sendMessageWithTemplate(any(User.class),
                eq(MailTemplate.REACTIVATE_USER));

        // Verify it's the correct users
        verify(mailSender, times(1)).sendMessageWithTemplate(eq(user1),
                eq(MailTemplate.REACTIVATE_USER));

        verify(mailSender, times(1)).sendMessageWithTemplate(eq(user4),
                eq(MailTemplate.REACTIVATE_USER));
    }
}
