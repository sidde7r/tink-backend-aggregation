package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.strings.StringUtils;

public class SEBSessionStorage {
    final SessionStorage sessionStorage;

    public SEBSessionStorage(SessionStorage storage) {
        sessionStorage = storage;
    }

    public void putUserInformation(UserInformation userInformation) {
        final String customerName = StringUtils.trimToNull(userInformation.getUserName());
        Preconditions.checkNotNull(customerName, "Did not get customer name.");
        sessionStorage.put(StorageKeys.CUSTOMER_NAME, customerName);

        final String customerNumber = Strings.emptyToNull(userInformation.getSebCustomerNumber());
        Preconditions.checkNotNull(customerNumber, "Did not get customer number.");
        sessionStorage.put(StorageKeys.CUSTOMER_NUMBER, customerNumber);

        final String userId = Strings.emptyToNull(userInformation.getShortUserId());
        Preconditions.checkNotNull(userId, "Did not get short user ID.");
        sessionStorage.put(StorageKeys.SHORT_USERID, userId);

        final String ssn = Strings.emptyToNull(userInformation.getSSN());
        Preconditions.checkNotNull(ssn, "Did not get SSN.");
        sessionStorage.put(StorageKeys.SSN, ssn);
    }

    public String getCustomerNumber() {
        return sessionStorage.get(StorageKeys.CUSTOMER_NUMBER);
    }
}
