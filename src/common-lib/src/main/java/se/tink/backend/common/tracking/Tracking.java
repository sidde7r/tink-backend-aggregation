package se.tink.backend.common.tracking;

public class Tracking {

    public static class Screen
    {
        public static final String FEED = "Feed";
    }

    public static class Action
    {
        public static final String BUTTON_PRESSED = "Button pressed";
    }

    public static class FeedActivity
    {
        //
        // Transaction. One or more transactions.
        public static final String TRANSACTION = "Transaction";
        //
        // Income. One or more income transactions.
        public static final String INCOME = "Income";
        //
        // Transfer. Internal transactions (two transactions; sender [negative] and recipient [positive])
        public static final String TRANSFER = "Transfer";
        //
        // Large expense. One transactions that is exceptionally large (compared to its category)
        public static final String LARGE_EXPENSE = "Large expense";
        //
        // Double charge. Two transactions with the same description, the same day, with the same amount.
        public static final String DOUBLE_CHARGE = "Double charge";
        //
        // Fraud
        public static final String FRAUD = "Fraud";
        //
        // Follow
        public static final String FOLLOW = "Follow";
        //
        // E-invoice
        public static final String E_INVOICE = "E-invoice";
        //
        // Unusual activity
        public static final String UNUSUAL_ACTIVITY = "Unusual activity";
        public static final String UNUSUAL_ACTIVITY_CATEGORY_HIGH = "High category activity";
        public static final String UNUSUAL_ACTIVITY_CATEGORY_LOW = "Low category activity";
        public static final String UNUSUAL_ACTIVITY_ACCOUNT = "Unusual account activity";
        //
        // Net income. Your net income for a certain amount of periods has reached a milestone.
        public static final String NET_INCOME = "Net income";
        //
        // Left to spend
        public static final String LEFT_TO_SPEND = "LeftToSpend";
        //
        // Account balance
        public static final String ACCOUNT_BALANCE = "Account balance";
        public static final String ACCOUNT_BALANCE_HIGH = "High account balance";
        public static final String ACCOUNT_BALANCE_LOW = "Low account balance";
        //
        // Weekly summary
        public static final String WEEKLY_SUMMARY = "Weekly summary";
        //
        // Monthly summary
        public static final String MONTHLY_SUMMARY = "Monthly summary";
        //
        // Categorization (suggest)
        public static final String CATEGORIZATION = "Categorization";
        //
        // Merchantization (suggest)
        public static final String MERCHANTIZATION = "Merchantization";
        //
        // Image share (i.e. Tink Selfie)
        public static final String IMAGE_SHARE = "Image share";
        //
        // Custom content activity (Markdown-based). Not supported by Android yet.
        public static final String CUSTOM = "Custom";
        //
        // Merchant heat map
        public static final String MERCHANT_HEAT_MAP = "Merchant heat map";
        //
        // Discover
        public static final String DISCOVER_BUDGETS = "Discover (budgets)";
        //
        // LEGACY/OBSOLETE
        //
        // Payment. Use 'Transaction' instead.
        public static final String PAYMENT = "Payment";
        //
        // Budget. Use 'Follow' instead.
        public static final String BUDGET = "Budget";
        //
        // DEVELOPMENT
        //
        // Location
        public static final String LOCATION = "Location";
        //
        // Document
        public static final String DOCUMENT = "Document";
        public static final String OFFER = "Offer";
        public static final String CASHBACK = "Cashback";
        public static final String LOOKBACK = "Lookback";
        public static final String BADGE = "Badge";
        public static final String SUGGEST = "Suggest";
        public static final String SUGGEST_PROVIDER = "Suggest Provider";
        public static final String TRANSACTION_MULTIPLE = "Transaction multiple";
        public static final String INCOME_MULTIPLE = "Income multiple";
        public static final String LARGE_EXPENSE_MULTIPLE = "Large expense multiple";

        // Application
        public static final String APPLICATION_MORTGAGE = "application/mortgage";
        public static final String APPLICATION_SAVINGS = "application/savings";
        public static final String APPLICATION_RESUME_MORTGAGE = "application-resume/mortgage";
        public static final String APPLICATION_RESUME_SAVINGS = "application-resume/savings";

    }

}
