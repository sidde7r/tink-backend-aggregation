package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.BusinessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;
import se.tink.libraries.strings.StringUtils;

public class SebSessionStorage {
    final SessionStorage sessionStorage;
    private final SebBaseConfiguration sebConfiguration;

    public SebSessionStorage(SessionStorage storage, SebBaseConfiguration sebConfiguration) {
        sessionStorage = storage;
        this.sebConfiguration = sebConfiguration;
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

        // For business we don't get SSN
        if (!sebConfiguration.isBusinessAgent()) {
            final String ssn = userInformation.getSSN();
            if (Strings.isNullOrEmpty(ssn)) {
                throw new IllegalStateException("Did not get customer SSN.");
            }
            sessionStorage.put(StorageKeys.SSN, ssn);
        }
    }

    public void putCompanyInformation(BusinessEntity companyInformation) {
        final String companyName = StringUtils.trimToNull(companyInformation.getCompanyName());
        if (Strings.isNullOrEmpty(companyName)) {
            throw new IllegalStateException("Did not get company name.");
        }
        sessionStorage.put(StorageKeys.COMPANY_NAME, companyName);

        final String companyNumber = StringUtils.trimToNull(companyInformation.getCompanyNumber());
        if (Strings.isNullOrEmpty(companyNumber)) {
            throw new IllegalStateException("Did not get company number.");
        }
        sessionStorage.put(StorageKeys.CUSTOMER_NUMBER, companyNumber);
    }

    public void putCardHandle(String uniqueId, String handle) {
        if (Strings.isNullOrEmpty(uniqueId)) {
            throw new IllegalStateException("Did not get card uniqueId");
        }
        if (Strings.isNullOrEmpty(handle)) {
            throw new IllegalStateException("Did not get card handle");
        }
        sessionStorage.put(StorageKeys.CREDIT_CARD_ACCOUNT_HANDLE_PREFIX + uniqueId, handle);
    }

    public String getCardHandle(String uniqueId) {
        return sessionStorage.get(StorageKeys.CREDIT_CARD_ACCOUNT_HANDLE_PREFIX + uniqueId);
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
