package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.RequestEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LoginResponseEntity extends BaseMobileResponseEntity {
    private SysDataEntity sysData;
    private CustomerEntity customer;
    private List<RequestEntity> requests;
    private List<ServiceEntity> services;
    private OptInsEntity optIns;

    public SysDataEntity getSysData() {
        return this.sysData;
    }

    public CustomerEntity getCustomer() {
        return this.customer;
    }

    public List<RequestEntity> getRequests() {
        return this.requests;
    }

    public List<ServiceEntity> getServices() {
        return this.services;
    }

    public OptInsEntity getOptIns() {
        return this.optIns;
    }

    public Optional<HolderName> getCustomerHolderName() {
        return Optional.ofNullable(this.customer).flatMap(CustomerEntity::getHolderName);
    }

    public Optional<URL> findAccountRequest() {
        return findRequest(RequestEntity::isGetAccounts);
    }

    public Optional<URL> findPendingPaymentsRequest() {
        return findRequest(RequestEntity::isGetPendingPayments);
    }

    public Optional<URL> findTrustedBeneficiariesRequest() {
        return findRequest(RequestEntity::isTrustedBenficiariesRequest);
    }

    public Optional<URL> findValidateTransferRequest() {
        return findRequest(RequestEntity::isValidateTransfer);
    }

    public Optional<URL> findValidateTrustedTransferRequest() {
        return findRequest(RequestEntity::isValidateTrustedTransfer);
    }

    public Optional<URL> findValidateThirdTransferRequest() {
        return findRequest(RequestEntity::isValidateThirdPartyTransfer);
    }

    private Optional<URL> findRequest(Predicate<RequestEntity> requestPredicate) {
        return Optional.ofNullable(requests)
                .map(Collection::stream)
                .flatMap(requests -> requests
                        .filter(requestPredicate)
                        .findFirst())
                .map(RequestEntity::asSSORequest);
    }

    public Optional<String> findCreditCardsRequestUrl() {
        return findRequestUrlForLogging(RequestEntity::isCreditCards);
    }

    public Optional<String> findCreditCardTransactionsRequestUrl() {
        return findRequestUrlForLogging(RequestEntity::isCreditCardTransactions);
    }

    private Optional<String> findRequestUrlForLogging(Predicate<RequestEntity> requestPredicate) {
        return Optional.ofNullable(this.requests)
                .map(Collection::stream)
                .flatMap(requests -> requests
                        .filter(requestPredicate)
                        .findFirst())
                .map(RequestEntity::getUrl);
    }
}
