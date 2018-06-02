package se.tink.backend.export.factory;

import se.tink.backend.export.helper.UserNotFoundException;
import se.tink.backend.export.model.Booleans;
import se.tink.backend.export.model.AccountHistory;
import se.tink.backend.export.model.Accounts;
import se.tink.backend.export.model.ApplicationEvents;
import se.tink.backend.export.model.Applications;
import se.tink.backend.export.model.Budgets;
import se.tink.backend.export.model.Consents;
import se.tink.backend.export.model.Credentials;
import se.tink.backend.export.model.PropertyEstimates;
import se.tink.backend.export.model.SavingsGoals;
import se.tink.backend.export.model.UserDetails;
import se.tink.backend.export.model.UserDevices;
import se.tink.backend.export.model.Documents;
import se.tink.backend.export.model.UserEvents;
import se.tink.backend.export.model.FacebookDetails;
import se.tink.backend.export.model.FraudDetails;
import se.tink.backend.export.model.InstrumentHistory;
import se.tink.backend.export.model.Instruments;
import se.tink.backend.export.model.Loans;
import se.tink.backend.export.model.UserLocations;
import se.tink.backend.export.model.PortfolioHistory;
import se.tink.backend.export.model.Portfolios;
import se.tink.backend.export.model.Properties;
import se.tink.backend.export.model.Transactions;
import se.tink.backend.export.model.Transfers;

public interface ExportObjectFactory {

    Booleans createExportBooleans(String userId);

    UserDetails createUserDetails(String userId);

    FacebookDetails createFacebookDetails(String userId);

    FraudDetails createFraudDetails(String userId);

    Consents createUserConsents(String userId);

    UserDevices createUserDevices(String userId);

    UserEvents createUserEvents(String userId);

    UserLocations createUserLocations(String userId);

    Applications createApplications(String userId);

    ApplicationEvents createApplicationEvents(String userId);

    Documents createDocuments(String userId);

    Properties createProperties(String userId);

    PropertyEstimates createPropertyEstimates(String userId);

    Credentials createCredentials(String userId);

    Loans createLoans(String userId);

    Accounts createAccounts(String userId);

    AccountHistory createAccountHistory(String userId);

    Transactions createTransactions(String userId);

    Transfers createTransfers(String userId);

    Portfolios createPortfolios(String userId);

    PortfolioHistory createPortfolioHistory(String userId);

    Instruments createInstruments(String userId);

    InstrumentHistory createInstrumentHistory(String userId);

    Budgets createBudgets(String userId);

    SavingsGoals createSavingsGoals(String userId);

    void validateUser(String userId) throws UserNotFoundException;

}
