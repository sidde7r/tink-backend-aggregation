package se.tink.backend.system.workers.training;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.lang3.StringUtils;
import rx.Observable;
import rx.functions.Action1;
import se.tink.backend.categorization.api.FastTextClassifierService;
import se.tink.backend.categorization.api.FastTextTrainerService;
import se.tink.backend.categorization.rpc.FastTextTrainRequest;
import se.tink.backend.categorization.rpc.FeedTrainingRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.log.LogUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FastTextModelTrainer {
    private static final LogUtils log = new LogUtils(FastTextModelTrainer.class);
    private static final int numberOfUsers = 10000;
    private CategoryRepository categoryRepository;
    private FastTextServiceFactoryProvider fastTextServiceFactoryProvider;
    private TransactionDao transactionDao;
    private UserRepository userRepository;

    public FastTextModelTrainer(CategoryRepository categoryRepository, FastTextServiceFactoryProvider fastTextServiceFactoryProvider,
                                TransactionDao transactionDao, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.fastTextServiceFactoryProvider = fastTextServiceFactoryProvider;
        this.transactionDao = transactionDao;
        this.userRepository = userRepository;
    }

    public void trainFastTextModel() {
        final AtomicInteger counter = new AtomicInteger(0);
        ImmutableList<Category> categories = new ClusterCategories(categoryRepository.findAll()).get();
        Map<String, String> categoriesById = categories.stream().collect(Collectors.toMap(Category::getId, Category::getCode));
        FastTextTrainerService fastTextTrainerService = fastTextServiceFactoryProvider.get().getFastTextTrainerService();
        FastTextClassifierService fastTextClassifierService = fastTextServiceFactoryProvider.get().getFastTextClassifierService();
        String modelPath = fastTextTrainerService.createTraining().getLocation();
        Observable<User> userObservable = userRepository.streamAll();
        userObservable
                .take(numberOfUsers)
                .flatMap(u -> Observable.from(transactionDao.findAllByUserId(u.getId()).stream()
                        .filter(this::isValidTransaction).collect(Collectors.toList())))
                .map(t -> {
                    String label = categoriesById.get(t.getCategoryId());
                    return new FeedTrainingRequest(Collections.singletonList(label), t.getDescription());
                })
                .subscribe(feedTrainingData(fastTextTrainerService, modelPath, counter));

        try {
            fastTextClassifierService.train(new FastTextTrainRequest(String.format("model-%s", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)), modelPath));
        } catch(UniformInterfaceException uie) {
            log.error(String.format("%s\n%s",uie.getMessage(), uie.getResponse()), uie);
        }
        
        log.info(String.format("done training fast text model with %s transactions.", counter.get()));
    }

    private Action1<FeedTrainingRequest> feedTrainingData(FastTextTrainerService fastTextTrainerService, String modelPath, AtomicInteger transactionsCounter) {
        return f -> {
            log.info(String.format("sending request %s", modelPath));
            fastTextTrainerService.feedTraining(modelPath, f);
            log.info(String.format("request sent %s %s", f.getDescription(), f.getLabels()));
            transactionsCounter.incrementAndGet();
        };
    }

    private boolean isValidTransaction(Transaction transaction) {
        return !StringUtils.isEmpty(transaction.getDescription())
                && !StringUtils.isEmpty(transaction.getCategoryId());
    }
}
