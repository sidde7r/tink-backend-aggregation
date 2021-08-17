package se.tink.backend.aggregation.agents.banks.sbab;

public class SBABConstants {
    public static final String INTEGRATION_NAME = "sbab";
    public static final String TRUE = "true";

    public static class Url {
        public static final String HOST = "https://secure.sbab.se";

        public static final String GRAPGQL_URL = HOST + "/beta/api/bb-bff/graphql";
        public static final String IDENTITY_URL =
                HOST + "/beta/api/bb-bff/document/dataportability";

        public static final String OVERVIEW_URL = HOST + "/beta?rev=1621409868887";
        public static final String BANKID_BASE_URL = HOST + "/ib-login/api/v1/";
    }

    public static class HeaderKeys {
        public static final String CSRF_TOKEN = "CSRF-Token";
        public static final String USER_AGENT = "User-Agent";
    }

    public static class QueryParamKeys {
        public static final String DEP = "dep";
        public static final String AUTH_MECH = "auth_mech";
        public static final String AUTH_DEVICE = "auth_device";
        public static final String REV = "rev";
    }

    public static class QueryParamValues {
        public static final String DEP = "privat";
        public static final String AUTH_MECH = "PW_MBID";
        public static final String AUTH_DEVICE = "SAME";
    }

    public static class BankId {
        public static final int BANKID_MAX_ATTEMPTS = 90;

        public static final String CANCELED = "CANCELED";
        public static final String STARTED = "STARTED";
        public static final String SUCCESS = "SUCCESS";
        public static final String ALREADY_IN_PROGRESS = "SIGN_ALREADY_IN_PROGRESS";
    }

    public static class ErrorText {
        public static final String HTTP_ERROR = "Failed: Http error code:";
        public static final String HTTP_MESSAGE = ", message: ";
    }

    public static class OperationNames {
        public static final String SAVINGS_ACCOUNTS_QUERY = "SavingsAccountsQuery";
        public static final String LOAN_ACCOUNTS_QUERY = "CollateralsQuery";
        public static final String BLANCO_LOAN_ACCOUNTS_QUERY = "LoansQuery";
        public static final String SAVINGS_ACCOUNTS_DETAILS_QUERY = "SavingsOverviewQuery";
        public static final String LOAN_DETAILS_QUERY = "LoansOverviewQuery";
        public static final String AUTH_STATUS_POLL_QUERY = "AuthStatusPollQuery";
    }

    public static class Query {
        public static final String SAVINGS_ACCOUNTS_DETAILS =
                "query SavingsOverviewQuery($accountNumber: String) {\n  staticInformation {\n    savingsGoalCategories {\n      code\n      name\n      sortIndex\n      __typename\n    }\n    __typename\n  }\n  user {\n    id\n    savingsAccount(accountNumber: $accountNumber) {\n      id\n      name\n      number\n      description\n      balance\n      availableForWithdrawal\n      status\n      accountType\n      interestRate\n      accruedInterestCredit\n      openDate\n      closeDate\n      isActionGranted\n      isTaxAccount\n      mandateType\n      accountHolders {\n        fullName\n        orgPersNbr\n        __typename\n      }\n      authorities {\n        id\n        fullName\n        orgPersNbr\n        __typename\n      }\n      savingsGoal {\n        category {\n          code\n          name\n          sortIndex\n          __typename\n        }\n        amount\n        targetDate\n        __typename\n      }\n      transfers {\n        completed {\n          amount\n          balance\n          transferId\n          transferStatus\n          transferType\n          releaseDate\n          postingDate\n          accountNumberFrom\n          accountNumberTo\n          bankNameFrom\n          bankNameTo\n          narrativeFrom\n          narrativeTo\n          recurringTransfer\n          futureTransfer\n          __typename\n        }\n        upcoming {\n          amount\n          balance\n          transferId\n          transferStatus\n          transferType\n          releaseDate\n          postingDate\n          accountNumberFrom\n          accountNumberTo\n          bankNameFrom\n          bankNameTo\n          narrativeFrom\n          narrativeTo\n          recurringTransfer\n          futureTransfer\n          __typename\n        }\n        failed {\n          amount\n          balance\n          transferId\n          transferStatus\n          transferType\n          releaseDate\n          postingDate\n          accountNumberFrom\n          accountNumberTo\n          bankNameFrom\n          bankNameTo\n          narrativeFrom\n          narrativeTo\n          recurringTransfer\n          futureTransfer\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
        public static final String SAVINGS_ACCOUNTS =
                "query SavingsAccountsQuery($filter: SavingsAccountsFilter) {\n  user {\n    id\n    savingsAccounts(filter: $filter) {\n      id\n      name\n      description\n      balance\n      availableForWithdrawal\n      status\n      accountType\n      interestRate\n      __typename\n    }\n    __typename\n  }\n}\n";
        public static final String BLANCO_LOAN_ACCOUNTS =
                "query LoansQuery {\n  user {\n    id\n    loans {\n      id\n      blanco {\n        id\n        totalAmount\n        list {\n          loanAmount\n          loanNumber\n          loanType\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
        public static final String LOAN_DETAILS =
                "query LoansOverviewQuery($loanNumber: String) {\n  user {\n    id\n    loan(loanNumber: $loanNumber) {\n      loanNumber\n      legacyLoanNumber\n      loanAmount\n      originalLoanAmount\n      loanType\n      loanStatus\n      paymentDate\n      numberOfBorrowers\n      discountType\n      participationShare\n      borrowers {\n        personalIdentityNumber\n        participationShare\n        fullName\n        __typename\n      }\n      insurance {\n        percentage\n        type\n        company\n        __typename\n      }\n      loanTerms {\n        interestRate\n        amortizationAmount\n        fixedInterestPeriodMonths\n        interestResetDate\n        changeOfConditionDate\n        paymentTerm\n        amortizationType\n        __typename\n      }\n      loanObject {\n        designation\n        municipalityName\n        objectId\n        __typename\n      }\n      offer {\n        isBindable\n        activeErrands\n        alternatives {\n          bindingPeriod\n          value\n          listPrice\n          untilDate\n          __typename\n        }\n        __typename\n      }\n      invoices {\n        bankgiroNumber\n        invoiceNumber\n        invoicePeriod\n        payDate\n        paymentDescription\n        totalAmount\n        settled\n        ocrNumber\n        dueDate\n        loanTypes\n        remainingAmount\n        loans {\n          loanType\n          loanNumber\n          amortizationAmount\n          interestAmount\n          totalAmount\n          invoiceLines {\n            lineNumber\n            invoiceLineType\n            typeDescriptionDetail\n            amount\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
        public static final String LOAN_ACCOUNTS =
                "query CollateralsQuery {\n  user {\n    id\n    loans {\n      id\n      mortgages {\n        id\n        collaterals {\n          totalAmount\n          cadastral\n          objectId\n          objectType\n          energyPerformance {\n            energyClass\n            __typename\n          }\n          mortgages {\n            loanNumber\n            legacyLoanNumber\n            loanAmount\n            originalLoanAmount\n            loanType\n            loanStatus\n            discountType\n            loanTerms {\n              interestRate\n              fixedInterestPeriodMonths\n              __typename\n            }\n            invoices {\n              bankgiroNumber\n              invoiceNumbers\n              invoicePeriod\n              payDate\n              paymentDescription\n              totalAmount\n              settled\n              ocrNumber\n              dueDate\n              loanTypes\n              remainingAmount\n              loans {\n                loanType\n                loanNumber\n                amortizationAmount\n                interestAmount\n                totalAmount\n                invoiceLines {\n                  lineNumber\n                  invoiceLineType\n                  typeDescriptionDetail\n                  amount\n                  __typename\n                }\n                __typename\n              }\n              __typename\n            }\n            __typename\n          }\n          trustDeeds {\n            id\n            deedValue\n            mortgageLienPriority\n            objectId\n            nrlopobj\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
        public static final String AUTH_STATUS =
                "query AuthStatusPollQuery {\n  user {\n    id\n    __typename\n  }\n}\n";
    }
}
