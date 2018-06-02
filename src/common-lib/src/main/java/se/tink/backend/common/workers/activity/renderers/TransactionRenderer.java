package se.tink.backend.common.workers.activity.renderers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.FeedTransactionData;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;

public class TransactionRenderer extends BaseActivityRenderer {

    private static final TypeReference<List<Transaction>> TRANSACTION_LIST_TYPE_REFERENCE = new TypeReference<List<Transaction>>() {
    };
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public TransactionRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        if (isPluralActivity(activity)) {
            return renderPlural(activity);
        } else {
            return renderSingle(activity);
        }
    }

    private String renderSingle(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);

        Transaction t = activity.getContent(Transaction.class);
        ActivityHeader headerData = new ActivityHeader();
        Icon iconSvg = getIconSVGForActivity(activity);

        Catalog catalog = context.getCatalog();

        double sum = t.getAmount();

        switch (activity.getType()) {
        case Activity.Types.LARGE_EXPENSE:
            headerData.setLeftHeader(context.getCatalog().getString("Large expense"));
            headerData.setLeftSubtext(t.getDescription());
            break;
        case Activity.Types.BANK_FEE:
            headerData.setLeftHeader(context.getCatalog().getString("Bank Fee"));
            headerData.setLeftSubtext(t.getDescription());
            break;
        default:
            headerData.setLeftHeader(t.getDescription());
            headerData.setLeftSubtext(context.getCategory(t.getCategoryId()).getDisplayName());
            break;
        }

        headerData.setRightHeader(I18NUtils.formatCurrency(sum, context.getUserCurrency(), context.getLocale()));
        headerData.setRightSubtext(I18NUtils.humanDateFormat(catalog, context.getLocale(), t.getDate()));
        headerData.setIcon(iconSvg);
        headerData.setDeepLink(getTransactionDeepLink(t, activity));

        params.put("headerData", headerData);
        params.put("transactionId", t.getId());

        return render(Template.ACTIVITIES_TRANSACTION_SINGLE_HTML, params);
    }

    private TransferFromTo fromTo(Activity a) {
        List<Transaction> transactions = a.getContent(TRANSACTION_LIST_TYPE_REFERENCE);

        String fromAccount = "";
        String toAccount = "";

        Transaction t1 = transactions.get(0);
        Transaction t2 = transactions.get(1);

        Account a1 = (t1 != null) ? context.getAccount(t1.getAccountId()) : null;
        Account a2 = (t2 != null) ? context.getAccount(t2.getAccountId()) : null;

        if (a1 == null || a2 == null) {
            // Probably a race condition when removing credentials, account doesnt exist.
            return null;
        }

        double t1Amount = t1 != null ? t1.getAmount() : 0;
        double t2Amount = t2 != null ? t2.getAmount() : 0;

        if (t1Amount < t2Amount) {
            fromAccount = a1.getName();
            toAccount = a2.getName();
        } else {
            fromAccount = a2.getName();
            toAccount = a1.getName();
        }

        TransferFromTo fromTo = new TransferFromTo();
        fromTo.setFrom(fromAccount);
        fromTo.setTo(toAccount);

        return fromTo;
    }

    private String getTransactionDeepLink(Transaction transaction, Activity activity) {
        return deepLinkBuilderFactory.transaction(transaction.getId()).withSource(getTrackingLabel(activity)).build();
    }

    private FeedTransactionData getTransactionHeaderData(Transaction t, Activity activity) {
        Catalog catalog = context.getCatalog();
        Icon icon = setupIconSVGForTransaction(t, activity);
        double sum = t.getAmount();

        FeedTransactionData headerData = new FeedTransactionData();
        headerData.setLeftHeader(t.getDescription());
        headerData.setRightHeader(I18NUtils.formatCurrency(sum, context.getUserCurrency(), context.getLocale()));
        headerData.setLeftSubtext(context.getCategory(t.getCategoryId()).getDisplayName());
        headerData.setRightSubtext(I18NUtils.humanDateFormat(catalog, context.getLocale(), t.getDate()));
        headerData.setIcon(icon);
        headerData.setTransaction(t);
        headerData.setDeepLink(getTransactionDeepLink(t, activity));

        return headerData;
    }

    private String renderPlural(Activity activity) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);

        ActivityHeader headerData = new ActivityHeader();
        double sum = 0d;
        StringBuilder descriptions = new StringBuilder();
        Catalog catalog = context.getCatalog();
        String leftHeader = null;

        Icon iconSvg = getIconSVGForActivity(activity);

        List<Transaction> transactionList = activity.getContent(TRANSACTION_LIST_TYPE_REFERENCE);
        List<FeedTransactionData> transactionsRows = Lists.newArrayList();
        int count = transactionList.size();
        TransferFromTo fromTo = null;

        for (int i = 0; i < count; i++) {
            Transaction t = transactionList.get(i);

            sum += t.getAmount();
            if (!activity.getType().equals(Activity.Types.DOUBLE_CHARGE)
                    && !activity.getType().equals(Activity.Types.TRANSFER)) {
                descriptions.append(t.getDescription());
            }
            transactionsRows.add(getTransactionHeaderData(t, activity));

            if (i == count - 1) {
                switch (activity.getType()) {
                case Activity.Types.INCOME_MULTIPLE:
                    leftHeader = Catalog.format(catalog.getString("{0} incomes"), count);
                    break;
                case Activity.Types.TRANSACTION_MULTIPLE:
                    leftHeader = Catalog.format(catalog.getString("{0} expenses"), count);
                    break;
                case Activity.Types.TRANSFER:
                    leftHeader = Catalog.format(catalog.getString("Transfer"), count);
                    sum = Math.abs(t.getAmount());
                    fromTo = fromTo(activity);
                    break;
                case Activity.Types.LARGE_EXPENSE_MULTIPLE:
                    if (count == 1) {
                        leftHeader = Catalog.format(catalog.getString("{0} large expense"), count);
                    } else {
                        leftHeader = Catalog.format(catalog.getString("{0} large expenses"), count);
                    }
                    break;
                case Activity.Types.BANK_FEE_MULTIPLE:
                    leftHeader = Catalog.format(catalog.getString("{0} bank fees"), count);
                    break;
                case Activity.Types.DOUBLE_CHARGE:
                    leftHeader = catalog.getString("Double charge");
                    descriptions.append(t.getDescription());
                    sum = Math.abs(t.getAmount());
                    break;
                }
            } else if (!activity.getType().equals(Activity.Types.DOUBLE_CHARGE)
                    && !activity.getType().equals(Activity.Types.TRANSFER)) {
                descriptions.append(", ");
            }
        }

        headerData.setLeftHeader(leftHeader);
        headerData.setRightHeader(I18NUtils.formatCurrency(sum, context.getUserCurrency(), context.getLocale()));
        headerData.setLeftSubtext(descriptions.toString());

        Date firstDate = transactionList.get(0).getDate();
        Date lastDate = transactionList.get(transactionList.size() - 1).getDate();

        String dateString;
        if (I18NUtils.isSameDay(firstDate, lastDate)) {
            dateString = I18NUtils.humanDateFormat(context.getCatalog(), context.getLocale(), firstDate);
        } else {
            dateString = Catalog.format("{0} - {1}",
                    I18NUtils.humanDateShortFormat(context.getCatalog(), context.getLocale(), lastDate),
                    I18NUtils.humanDateShortFormat(context.getCatalog(), context.getLocale(), firstDate));
        }

        headerData.setRightSubtext(dateString);
        headerData.setIcon(iconSvg);

        headerData.setDeepLink(deepLinkBuilderFactory.tracking().withSource(getTrackingLabel(activity)).build());

        params.put("fromTo", fromTo);
        params.put("headerData", headerData);
        params.put("innerTemplate", "transaction-plural");
        params.put("transactions", transactionsRows);

        if (activity.getType().equals(Activity.Types.TRANSFER)) {
            return render(Template.ACTIVITIES_TRANSFER_HTML, params);
        } else {
            return render(Template.ACTIVITIES_TRANSACTION_PLURAL_COLLAPSED_HTML, params);
        }

    }

    private Icon setupIconSVGForTransaction(Transaction t, Activity activity) {
        Icon icon = new Icon();
        String categoryCode = getCategoryCode(t.getCategoryId());

        icon.setColorType(getIconColorType(categoryCode, t.getCategoryType()));
        char rawIcon;
        if (v2) {
           rawIcon = TinkIconUtils.getV2CategoryIcon(categoryCode);
        } else {
            rawIcon = TinkIconUtils.getV1CategoryIcon(categoryCode);
        }
        icon.setChar(rawIcon);

        if (activity.getType().equals(Activity.Types.BANK_FEE_MULTIPLE)) {
            icon.setColorType(Icon.IconColorTypes.CRITICAL);
            if (v2) {
                icon.setChar(TinkIconUtils.IconsV2.ALERT);
            } else {
                icon.setChar(TinkIconUtils.Icons.ALERT);
            }
        }

        return icon;
    }

    private Icon getIconSVGForActivity(Activity a) {
        Icon icon = new Icon();

        icon.setColorType(getIconColor(a));
        icon.setChar(getIconChar(a));
        return icon;
    }

    private boolean isPluralActivity(Activity activity) {
        switch (activity.getType()) {
        case Activity.Types.INCOME_MULTIPLE:
        case Activity.Types.TRANSACTION_MULTIPLE:
        case Activity.Types.TRANSFER:
        case Activity.Types.DOUBLE_CHARGE:
        case Activity.Types.LARGE_EXPENSE_MULTIPLE:
        case Activity.Types.BANK_FEE_MULTIPLE:
            return true;
        default:
            return false;
        }
    }

    private char getIconChar(Activity a) {
        switch (a.getType()) {
        case Activity.Types.TRANSFER:
            if (v2) {
                return TinkIconUtils.IconsV2.TRANSFER;
            }
            return TinkIconUtils.Icons.TRANSFER;
        case Activity.Types.TRANSACTION_MULTIPLE:
            if (v2) {
                return TinkIconUtils.IconsV2.TRANSFER;
            }
            return TinkIconUtils.Icons.EXPENSES;
        case Activity.Types.INCOME_MULTIPLE:
            if (v2) {
                return TinkIconUtils.IconsV2.OTHERINCOME;
            }
            return TinkIconUtils.Icons.INCOME;
        case Activity.Types.DOUBLE_CHARGE:
            if (v2) {
                return TinkIconUtils.IconsV2.DOUBLETRANSACTION;
            }
            return TinkIconUtils.Icons.DOUBLE_CHARGE;
        case Activity.Types.LARGE_EXPENSE:
        case Activity.Types.LARGE_EXPENSE_MULTIPLE:
            if (v2) {
                return TinkIconUtils.IconsV2.ALERT;
            }
            return TinkIconUtils.Icons.LARGE_EXPENSE;
        case Activity.Types.INCOME:
        case Activity.Types.TRANSACTION:
            if (v2) {
                return TinkIconUtils
                        .getV2CategoryIcon(getCategoryCode(a.getContent(Transaction.class).getCategoryId()));
            }
            return TinkIconUtils
                    .getV1CategoryIcon(getCategoryCode(a.getContent(Transaction.class).getCategoryId()));
        case Activity.Types.BANK_FEE:
        case Activity.Types.BANK_FEE_MULTIPLE:
            if (v2) {
                return TinkIconUtils.IconsV2.ALERT;
            }
            return TinkIconUtils.Icons.BANK_FEE;
        default:
            if (v2) {
                return TinkIconUtils.IconsV2.ALERT;
            }
            return TinkIconUtils.Icons.ALERT;
        }
    }

    public static class TransferFromTo {
        private String from;
        private String to;

        private TransferFromTo() {
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

    private String getCategoryCode(String id) {
        for (Category c : context.getCategories()) {
            if (c.getId().equals(id)) {
                return c.getCode();
            }
        }
        return null;
    }

    public String getIconColorType(String categoryCode, CategoryTypes t) {
        if (categoryCode.equals(context.getCategoryConfiguration().getExpenseUnknownCode())) {
            return Icon.IconColorTypes.UNCATEGORIZED;
        }

        switch (t) {
        case TRANSFERS:
            return Icon.IconColorTypes.TRANSFER;
        case EXPENSES:
            return Icon.IconColorTypes.EXPENSE;
        case INCOME:
            return Icon.IconColorTypes.INCOME;
        default:
            return Icon.IconColorTypes.INFO;
        }
    }

    public String getIconColor(Activity a) {
        if (isPluralActivity(a)) {
            switch (a.getType()) {
            case Activity.Types.TRANSFER:
                return Icon.IconColorTypes.TRANSFER;
            case Activity.Types.TRANSACTION_MULTIPLE:
                return Icon.IconColorTypes.EXPENSE;
            case Activity.Types.INCOME_MULTIPLE:
                return Icon.IconColorTypes.INCOME;
            case Activity.Types.DOUBLE_CHARGE:
            case Activity.Types.BANK_FEE_MULTIPLE:
            case Activity.Types.LARGE_EXPENSE_MULTIPLE:
                return Icon.IconColorTypes.CRITICAL;
            default:
                throw new RuntimeException(String.format("No icon color defined for type '%s'.", a.getType()));
            }
        } else {

            // Always use critical color for bank fees and large expense
            switch (a.getType()) {
            case Activity.Types.BANK_FEE:
            case Activity.Types.LARGE_EXPENSE:
                return Icon.IconColorTypes.CRITICAL;
            default:
                Transaction t = a.getContent(Transaction.class);
                String categoryCode = getCategoryCode(t.getCategoryId());
                return getIconColorType(categoryCode, t.getCategoryType());
            }
        }
    }
}
