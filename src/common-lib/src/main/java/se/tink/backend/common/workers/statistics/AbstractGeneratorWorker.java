package se.tink.backend.common.workers.statistics;

import java.util.List;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Category;

public abstract class AbstractGeneratorWorker {

    protected final AccountRepository accountRepository;
    protected final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    protected final CategoryRepository categoryRepository;
    protected final CredentialsRepository credentialsRepository;
    protected final EventRepository eventRepository;
    protected final LoanDataRepository loanDataRepository;
    protected final MarketRepository marketRepository;
    protected final PostalCodeAreaRepository postalCodeAreaRepository;
    protected final ServiceContext serviceContext;
    protected final StatisticDao statisticDao;
    protected final TransactionDao transactionDao;
    protected final UserEventRepository userEventRepository;
    protected final UserRepository userRepository;
    protected final UserDemographicsRepository userDemographicsRepository;
    protected final UserFacebookFriendRepository userFacebookFriendRepository;
    protected final UserFacebookProfileRepository userFacebookProfileRepository;
    protected final UserOriginRepository userOriginRepository;
    protected final UserStateRepository userStateRepository;
    protected final PropertyRepository propertyRepository;

    protected final List<Category> categories;

    public AbstractGeneratorWorker(ServiceContext serviceContext) {

        this.serviceContext = serviceContext;

        eventRepository = serviceContext.getRepository(EventRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        statisticDao = serviceContext.getDao(StatisticDao.class);
        marketRepository = serviceContext.getRepository(MarketRepository.class);
        postalCodeAreaRepository = serviceContext.getRepository(PostalCodeAreaRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        userDemographicsRepository = serviceContext.getRepository(UserDemographicsRepository.class);
        userOriginRepository = serviceContext.getRepository(UserOriginRepository.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        userEventRepository = serviceContext.getRepository(UserEventRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        accountBalanceHistoryRepository = serviceContext.getRepository(AccountBalanceHistoryRepository.class);
        userFacebookFriendRepository = serviceContext.getRepository(UserFacebookFriendRepository.class);
        userFacebookProfileRepository = serviceContext.getRepository(UserFacebookProfileRepository.class);
        categories = categoryRepository.findLeafCategories();
        propertyRepository = serviceContext.getRepository(PropertyRepository.class);
    }
}
