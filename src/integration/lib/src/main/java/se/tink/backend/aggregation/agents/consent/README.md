# Context
Nowadays, consent management is no-existent at Tink. We ask for as much as we can and it starts to be a no-go for more and more customers. This was especially visible for Account Check product. Technically, it is a one-off limited aggregation, so we definitely don't need any transactions nor other extra information.

To address above the following "library" have been built. Mostly to group everything in one place, but also to provide some shortcuts by providing most common patterns. For now we are forced to base mappings on RefreshableItems, which is far from ideal approach. In the future Aggregation Core will provide our internal Consent model (or something else e.g information about which Tink product is in-use). Hopefully, we will be able to limit the source code adjustments to scope of this lib when this happens.

Currently, 3 scenarios have to be covered for 2 Tink products - keep that in mind during tests:
- Account Check - input: CHECKING_ACCOUNTS + SAVING_ACCOUNTS + CREDITCARD_ACCOUNTS
- Account Check - input: CHECKING_ACCOUNTS + SAVING_ACCOUNTS + CREDITCARD_ACCOUNTS + IDENTITY DATA
- Transactions - input: All items (including  IDENTITY_DATA)

## Implementation

1. Create `*Scope` enum/class
    1. If scopes do not overlap between themselves - use `Scope` interface

    ```java
    public enum UkObScope implements Scope {
        READ_ACCOUNTS_DETAIL("ReadAccountsDetail"),
        READ_BALANCES("ReadBalances"),
        READ_TRANSACTIONS_CREDITS("ReadTransactionsCredits"),
        READ_TRANSACTIONS_DEBITS("ReadTransactionsDebits"),
        READ_TRANSACTIONS_DETAIL("ReadTransactionsDetail"),
        READ_PARTY("ReadParty"),
        READ_PARTY_PSU("ReadPartyPSU"),
        READ_BENEFICIARIES_DETAIL("ReadBeneficiariesDetail"),
        READ_SCHEDULED_PAYMENTS_DETAIL("ReadScheduledPaymentsDetail");

        private final String value;

        UkObScope(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
    ```

   2. if scopes overlap between themselves (e.g accountsDetails / accountsWithBalances / accountsWithBalancesAndTransactions) and you will have to always return single one - implement `Weighted<>` interface, pass your `*Scope` enum into generic placeholder
        1. Define weight for your scopes - the higher the value the wider the scope
        2. Implement `extendIfNotAvailable` method, so that unavailable scope can be replaced with wider one

    ```java
    public enum RedsysScope implements Weighted<RedsysScope> {
        // Detailed consent model
        ACCOUNTS("accounts", 0),
        BALANCES("balances", 0),
        TRANSACTIONS("transactions", 0),
        
        // Global consent model
        AVAILABLE_ACCOUNTS("availableAccounts", 1),
        AVAILABLE_ACCOUNTS_WITH_BALANCES("availableAccountsWithBalances", 2),
        ALL_PSD2("allPsd2", 3);

        protected static final int MIN_EXPIRATION_DAYS = 0;
        protected static final int MAX_EXPIRATION_DAYS = 90;
        protected static final int MIN_DAILY_FREQUENCY = 1;
        protected static final int MAX_DAILY_FREQUENCY = 4;
        protected static final Map<Integer, RedsysScope> WEIGHT_MAP = new HashMap<>();

        static {
            for (RedsysScope scope : RedsysScope.values()) {
                WEIGHT_MAP.put(scope.getWeight(), scope);
            }
        }

        private final String jsonName;
        private final int weight;

        RedsysScope(String jsonName, int weight) {
            this.jsonName = jsonName;
            this.weight = weight;
        }

        @Override
        public RedsysScope extendIfNotAvailable(Set<RedsysScope> availableScopes) {
            RedsysScope scope = this;
            int weight = this.getWeight();

            while (!availableScopes.contains(scope)) {
                scope = WEIGHT_MAP.get(weight++);
            }

            return scope;
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return jsonName;
        }
    }
    ```

2. Create  `*ConsentGenerator`
    1. Implement `ConsentGenerator<>` interface - your output class should be passed to generic placeholder (e.g request body class or just set of enums/strings)
    2. Implement `ToScope` or `ToScopes` functional interface

       ```java
        private static final ToScopes<RefreshableItem, UkObScope> itemToScopes =
                    item -> {
                        switch (item) {
                            case CHECKING_ACCOUNTS:
                            case SAVING_ACCOUNTS:
                            case CREDITCARD_ACCOUNTS:
                            case LOAN_ACCOUNTS:
                            case INVESTMENT_ACCOUNTS:
                                return Sets.newHashSet(
                                        UkObScope.READ_ACCOUNTS_DETAIL,
                                        UkObScope.READ_BALANCES,
                                        UkObScope.READ_PARTY);
                            case CHECKING_TRANSACTIONS:
                            case SAVING_TRANSACTIONS:
                            case CREDITCARD_TRANSACTIONS:
                            case LOAN_TRANSACTIONS:
                            case INVESTMENT_TRANSACTIONS:
                                return Sets.newHashSet(
                                        UkObScope.READ_TRANSACTIONS_DETAIL,
                                        UkObScope.READ_TRANSACTIONS_DEBITS,
                                        UkObScope.READ_TRANSACTIONS_CREDITS);
                            case IDENTITY_DATA:
                                return Sets.newHashSet(UkObScope.READ_PARTY_PSU);
                            case LIST_BENEFICIARIES:
                            case TRANSFER_DESTINATIONS:
                                return Sets.newHashSet(UkObScope.READ_BENEFICIARIES_DETAIL);
                            default:
                                return Collections.emptySet();
                        }
                    };
       ```
       ```java
        private static final ToScope<RefreshableItem, RedsysScope> itemToScope =
                    item -> {
                        switch (item) {
                            case CHECKING_ACCOUNTS:
                            case SAVING_ACCOUNTS:
                            case CREDITCARD_ACCOUNTS:
                            case LOAN_ACCOUNTS:
                            case INVESTMENT_ACCOUNTS:
                                return RedsysScope.AVAILABLE_ACCOUNTS_WITH_BALANCES;
                            case CHECKING_TRANSACTIONS:
                            case SAVING_TRANSACTIONS:
                            case CREDITCARD_TRANSACTIONS:
                            case LOAN_TRANSACTIONS:
                            case INVESTMENT_TRANSACTIONS:
                            case IDENTITY_DATA:
                            case LIST_BENEFICIARIES:
                            case TRANSFER_DESTINATIONS:
                            default:
                                return RedsysScope.ALL_PSD2;
                        }
                    };
       ```
    3. Use suppliers
        1. Add `ScopesSupplier`/`WeightedScopeSupplier` by composition or create a new one 
        2. Use `ItemSupplier` to provide items as input collection to supplier mentioned above
        
        ```java
        private final ScopesSupplier<RefreshableItem, UkObScope> scopesSupplier;
    
        private UkObConsentGenerator(
                AgentComponentProvider componentProvider, Set<UkObScope> availableScopes) {
            this.scopesSupplier =
                    new ScopesSupplier<>(
                            ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                            availableScopes,
                            itemToScopes);
        }
        ```
   4. Generate desired output - you might need to add extra logic here
   ```java
    @Override
    public AccountPermissionRequest generate() {
        return AccountPermissionRequest.of(scopesSupplier.getStrings());
    }
   ```
   ```java
    @Override
    public ConsentRequestBody generate() {
        int daysUntilExpiration = RedsysScope.MIN_EXPIRATION_DAYS;
        int frequencyPerDay = RedsysScope.MIN_DAILY_FREQUENCY;
        boolean recurringIndicator = false;
    
        Set<RedsysScope> scopes = scopesProvider.get();
        AccessEntity accessEntity = new AccessEntity();
        if (scopes.contains(ACCOUNTS)) {
            accessEntity.setAccounts(accountInfoEntities);
        }
    
        if (scopes.contains(BALANCES)) {
            accessEntity.setBalances(accountInfoEntities);
        }
    
        if (scopes.contains(TRANSACTIONS)) {
            accessEntity.setTransactions(accountInfoEntities);
            daysUntilExpiration = RedsysScope.MAX_EXPIRATION_DAYS;
            frequencyPerDay = RedsysScope.MAX_DAILY_FREQUENCY;
            recurringIndicator = true;
        }
    
        return ConsentRequestBody.builder()
                .access(accessEntity)
                .recurringIndicator(recurringIndicator)
                .validUntil(localDateTimeSource.now().toLocalDate().plusDays(daysUntilExpiration))
                .frequencyPerDay(frequencyPerDay)
                .combinedServiceIndicator(false)
                .build();
    }
   ```
   
### Extras
[List of possible scopes vs endpoints](https://docs.google.com/spreadsheets/d/13gzxn3LEWkFIAXxc04Ryq3t2EGzw5F6H9geR3cDOvBg/edit#gid=328942832)