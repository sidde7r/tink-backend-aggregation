package se.tink.backend.insights.app.generators;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateBudgetCloseInsightCommand;
import se.tink.backend.insights.app.queryservices.CategoryQueryService;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.FollowItemTransaction;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.transactions.TransactionQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BudgetOverspendGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(BudgetOverspendGenerator.class);

    private CommandGateway gateway;
    private CategoryQueryService categoryQueryService;
    private TransactionQueryService transactionQueryService;
    private FollowItemRepository followItemRepository;

    @Inject
    public BudgetOverspendGenerator(CommandGateway gateway,
            CategoryQueryService categoryQueryService,
            TransactionQueryService transactionQueryService,
            FollowItemRepository followItemRepository) {
        this.gateway = gateway;
        this.categoryQueryService = categoryQueryService;
        this.transactionQueryService = transactionQueryService;
        this.followItemRepository = followItemRepository;
    }

    @Override
    public void generateIfShould(UserId userId) {
        List<Category> categories = categoryQueryService.getAllCategories();
        String excludedCategoryId = categoryQueryService.getTransfersExcludeOtherId();

        List<InsightTransaction> transactions = transactionQueryService
                .getInsightTransactionForPeriodByUserId(userId);

        // TODO: make own value object (InsightFollowItem) of FollowItem
        List<FollowItem> followItems = followItemRepository.findByUserId(userId.value()).stream()
                .filter(f -> Objects.equal(f.getType(), FollowTypes.EXPENSES))
                .filter(f -> f.getFollowCriteria().getTargetAmount() != null)
                .collect(Collectors.toList());

        if (followItems.size() == 0) {
            log.info(userId, "No insight generated. Reason: No follow items of type expenses found");
            return;
        }

        List<FollowItemTransaction> followItemTransactions = Lists.newArrayList();

        for (FollowItem followItem : followItems) {
            // Fan out the category IDs and fetch the category IDs of any child-categories.
            final Set<String> followItemCategoryIds = Sets.newHashSet(SerializationUtils.deserializeFromString(
                    followItem.getCriteria(), ExpensesFollowCriteria.class).getCategoryIds());

            final Set<String> fannedOutFollowItemCategoryIds = Sets.newHashSet(Iterables.transform(
                    fanOutCategories(Iterables.filter(categories, c -> (followItemCategoryIds.contains(c.getId()))),
                            categories), Category::getId));

            // TODO: might have to also exclude certain accountId, not sure if necessary for budgets though
            List<InsightTransaction> followItemInsightTransactions = transactions.stream()
                    .filter(t -> fannedOutFollowItemCategoryIds.contains(t.getCategoryId()))
                    .filter(t -> !Objects.equal(excludedCategoryId, t.getCategoryId()))
                    .collect(Collectors.toList());

            FollowItemTransaction followItemTransaction = FollowItemTransaction
                    .of(followItemInsightTransactions, followItem.getFollowCriteria().getTargetAmount(),
                            followItem.getName());

            if (followItemTransaction.hasOverSpent()) {
                followItemTransactions.add(followItemTransaction);
            }
        }

        if (followItemTransactions.size() <= 0) {
            log.info(userId, "No insight generated. No followItemTransactions found");
            return;
        }

        CreateBudgetCloseInsightCommand command = new CreateBudgetCloseInsightCommand(userId,
                followItemTransactions);

        gateway.on(command);
    }

    private static Iterable<Category> fanOutCategories(Iterable<Category> categories,
            Iterable<Category> allCategories) {
        ImmutableListMultimap<String, Category> categoriesByParentId = Multimaps.index(
                Iterables.filter(allCategories, c -> (!Strings.isNullOrEmpty(c.getParent()))), c -> (c.getParent()));

        List<Category> fannedOutCategories = Lists.newArrayList();

        for (Category category : categories) {
            if (Strings.isNullOrEmpty(category.getSecondaryName())) {
                fannedOutCategories.addAll(categoriesByParentId.get(category.getId()));
            } else {
                fannedOutCategories.add(category);
            }
        }
        return fannedOutCategories;
    }
}
