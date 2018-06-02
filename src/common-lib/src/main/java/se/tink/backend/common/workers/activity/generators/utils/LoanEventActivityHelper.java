package se.tink.backend.common.workers.activity.generators.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEvent;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.utils.LoanUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;

public class LoanEventActivityHelper {


    private final double INTEREST_CHANGE_THRESHOLD = 0.00005; // Threshold set to 0.005%

    private LoanDAO loanDAO;
    private ProductDAO productDAO;

    private User user;
    private Map<String, Provider> loanProviders;
    private List<Account> accounts;
    private List<Credentials> credentials;
    private SwitchMortgageHelper switchMortgageHelper;
    private Catalog catalog;

    public LoanEventActivityHelper(ActivityGeneratorContext context){
        loanDAO = context.getServiceContext().getDao(LoanDAO.class);
        productDAO = context.getServiceContext().getDao(ProductDAO.class);
        switchMortgageHelper = new SwitchMortgageHelper(context);

        user = context.getUser();
        catalog = context.getCatalog();
        loanProviders = context.getProvidersByName();
        accounts = context.getAccounts();
        credentials = context.getCredentials();
    }

    public Map<String, List<Loan>> getLoansByAccountIds(List<Account> accounts) {
        return accounts.stream()
                .filter(a -> !a.isExcluded() && isLoan(a))
                .collect(Collectors.toMap(Account::getId, a -> loanDAO.getLoanDataByAccountId(a.getId())));
    }

    private static boolean isLoan(Account a) {
        return Objects.equals(a.getType(), AccountTypes.LOAN) ||
                Objects.equals(a.getType(), AccountTypes.MORTGAGE);
    }

    /**
     * Turn our lists of loans to filtered LoanEvents
     */
    public Map<String, List<LoanEvent>> createLoanEventsByIdsAndLocale(Map<String, List<Loan>> mappedLoanLists, String locale) {
        return Maps.transformValues(mappedLoanLists,
                loans -> {
                    List<LoanEvent> loanEvents = LoanUtils.createLoanEvents(loans, locale);
                    return filterChangedInterestLoanEvents(loanEvents);
                }
        );
    }

    private List<LoanEvent> filterChangedInterestLoanEvents(List<LoanEvent> loanEvents) {

        return loanEvents.stream()
                .filter(event -> Objects.equals(event.getType(), LoanEvent.Type.INTEREST_RATE_DECREASE)
                        || Objects.equals(event.getType(), LoanEvent.Type.INTEREST_RATE_INCREASE))
                .filter(event -> Math.abs(event.getInterestRateChange()) > INTEREST_CHANGE_THRESHOLD)
                .collect(Collectors.toList());
    }

    /**
     * Map all LoanEvents to their LocalDate.
     */
    public Map<LocalDate, List<LoanEvent>> getLoanEventsByDate(Map<String, List<LoanEvent>> mappedLoanEventLists) {
        ListMultimap<LocalDate, LoanEvent> loanEventsByDate = ArrayListMultimap.create();
        for (List<LoanEvent> events : mappedLoanEventLists.values()) {
            events.forEach(event -> loanEventsByDate.put(dateToLocalDate(event.getTimestamp()), event));
        }
        return Multimaps.asMap(loanEventsByDate);
    }

    /**
     * Map all LoanEvents to their credentials
     */
    public Map<String, List<LoanEvent>> splitLoanEventsByCredentials(List<LoanEvent> loanEvents){
        ListMultimap<String, LoanEvent> loanEventsByCredentials = ArrayListMultimap.create();
        for (LoanEvent event : loanEvents) {
            loanEventsByCredentials.put(event.getCredentials(), event);
        }
        return Multimaps.asMap(loanEventsByCredentials);
    }

    public List<LoanEvent> transformProvidersToDisplayName(List<LoanEvent> loanEvents) {
        loanEvents.forEach(event -> {
            String actualName = loanProviders.get(event.getProvider()).getDisplayName();
            event.setProvider(actualName);
        });

        return loanEvents;
    }

    /**
     * If we happen to have multiple data points for a loan account on the same date, only save the latest.
     */
    public List<LoanEvent> uniqueLoanEventsByAccountsOnDate(List<LoanEvent> loanEvents) {
        Map<String, LoanEvent> loanEventByAccountId = Maps.newHashMap();
        for (LoanEvent event : loanEvents) {
            if (loanEventByAccountId.containsKey(event.getAccountId())) {
                Date currentEventDate = event.getTimestamp();
                Date previousEventDate = loanEventByAccountId.get(event.getAccountId()).getTimestamp();

                // Replace with latest event
                if (currentEventDate.after(previousEventDate)) {
                    loanEventByAccountId.put(event.getAccountId(), event);
                }
            } else {
                loanEventByAccountId.put(event.getAccountId(), event);
            }
        }
        return Lists.newArrayList(loanEventByAccountId.values());
    }

    public boolean eligibleForSwitchMortgage(){
        UUID userId = UUIDUtils
                .fromTinkUUID(user.getId());

        ListMultimap<ProductType, ProductArticle> productsByType = FluentIterable
                .from(productDAO.findAllActiveArticlesByUserId(userId))
                .index(ProductArticle::getType);

        return switchMortgageHelper.canSaveMoneyBySwitchingMortgageProvider(
                accounts, credentials, productsByType.get(ProductType.MORTGAGE)
        );
    }

    public String getActivityTypeByValue(double value) {
        return value < 0 ? Activity.Types.LOAN_DECREASE : Activity.Types.LOAN_INCREASE;
    }

    public String getActivityTitleByValueAndPlural(double value, int count) {
        if (value < 0) {
            return catalog.getPluralString("Your rate has decreased", "Your rates have decreased", count);
        } else {
            return catalog.getPluralString("Your rate has increased", "Your rates have increased", count);
        }
    }

    private LocalDate dateToLocalDate(Date date){
        return LocalDate.from(date.toInstant().atZone(ZoneId.of("CET")));
    }
}
