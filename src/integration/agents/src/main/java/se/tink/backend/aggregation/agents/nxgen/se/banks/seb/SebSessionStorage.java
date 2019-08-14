package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;
import se.tink.libraries.strings.StringUtils;

public class SebSessionStorage {
    final SessionStorage sessionStorage;

    public SebSessionStorage(SessionStorage storage) {
        sessionStorage = storage;
    }

    public void putUserInformation(UserInformation userInformation) {
        final String customerName = StringUtils.trimToNull(userInformation.getUserName());
        if (Strings.isNullOrEmpty(customerName)) {
            throw new IllegalStateException("Did not get customer name.");
        }
        sessionStorage.put(StorageKeys.CUSTOMER_NAME, customerName);

        final String customerNumber = userInformation.getSebCustomerNumber();
        if (Strings.isNullOrEmpty(customerNumber)) {
            throw new IllegalStateException("Did not get customer number.");
        }
        sessionStorage.put(StorageKeys.CUSTOMER_NUMBER, customerNumber);

        final String userId = userInformation.getShortUserId();
        if (Strings.isNullOrEmpty(userId)) {
            throw new IllegalStateException("Did not get short user ID.");
        }
        sessionStorage.put(StorageKeys.SHORT_USERID, userId);

        final String ssn = userInformation.getSSN();
        if (Strings.isNullOrEmpty(ssn)) {
            throw new IllegalStateException("Did not get customer SSN.");
        }
        sessionStorage.put(StorageKeys.SSN, ssn);
    }

    public String getCustomerNumber() {
        return sessionStorage.get(StorageKeys.CUSTOMER_NUMBER);
    }

    private String getCustomerName() {
        return sessionStorage.get(StorageKeys.CUSTOMER_NAME);
    }

    private String getSSN() {
        return sessionStorage.get(StorageKeys.SSN);
    }

    public IdentityData getIdentityData() {
        return SeIdentityData.of(getCustomerName(), getSSN());
    }
}
