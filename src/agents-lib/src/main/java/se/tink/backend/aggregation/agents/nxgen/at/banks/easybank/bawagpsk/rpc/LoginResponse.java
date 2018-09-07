package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Disposer;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Failure;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Product;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Products;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ResponseMessage;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public final class LoginResponse {
    private Envelope envelope;

    public LoginResponse(final Envelope envelope) {
        this.envelope = envelope;
    }

    public boolean requestWasSuccessful() {
        return Optional.ofNullable(envelope.getBody())
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getOk).isPresent();
    }

    public boolean incorrectCredentials() {
        List<String> list = Optional.ofNullable(envelope.getBody())
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getFailure)
                .map(Failure::getResponseMessageList).orElse(new ArrayList<>())
                .stream()
                .map(ResponseMessage::getCode)
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            return list.get(0)
                    .trim().equalsIgnoreCase(BawagPskConstants.MESSAGES.INCORRECT_CREDENTIALS);
        }

        return false;
    }

    public boolean accountIsLocked() {

        List<String> list = Optional.ofNullable(envelope.getBody())
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getFailure)
                .map(Failure::getResponseMessageList).orElse(new ArrayList<>())
                .stream()
                .map(ResponseMessage::getCode)
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            return list.get(0) // TODO assert one and only one element
                    .trim().equalsIgnoreCase(BawagPskConstants.MESSAGES.ACCOUNT_LOCKED);
        }

        return false;
    }

    public String getServerSessionID() {
        return envelope.getBody().getLoginResponseEntity().getOk().getServerSessionID();
    }

    public String getQid() {
        return envelope.getBody().getLoginResponseEntity().getOk().getQid();
    }

    public ProductID getProductId(final String accountNumber) {
        return envelope.getBody().getLoginResponseEntity().getOk()
                .getDisposer()
                .getProducts()
                .getProductList().stream()
                .filter(product -> product.getAccountNumber().equals(accountNumber))
                .map(Product::getProductID)
                .collect(Collectors.toList())
                .get(0); // TODO assert one and only one element
    }

    public List<ProductID> getProductIdList() {
        return getProductList().stream()
                .map(Product::getProductID)
                .collect(Collectors.toList());
    }

    private List<Product> getProductList() {
        return Optional.ofNullable(envelope)
                .map(Envelope::getBody)
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getOk)
                .map(OK::getDisposer)
                .map(Disposer::getProducts)
                .map(Products::getProductList)
                .map(Stream::of).orElse(Stream.empty())
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @return A collection of invalid IBANs for the accounts that have one.
     */
    public Collection<IbanIdentifier> getInvalidIbans() {
        return getProductIdList().stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getIban() != null)
                .filter(v -> v.getFinancialInstitute() != null)
                .filter(v -> v.getFinancialInstitute().getBIC() != null)
                .map(LoginResponse::getIban)
                .filter(iban -> !iban.isValid())
                .collect(Collectors.toList());
    }

    private static IbanIdentifier getIban(final ProductID productID) {
        return new IbanIdentifier(productID.getFinancialInstitute().getBIC().trim(), productID.getIban().trim());
    }

    public Collection<TransactionalAccount> toTransactionalAccounts(final Map<String, Amount> accountNoToBalance) {
        return getProductList().stream()
                .filter(p -> accountNoToBalance.containsKey(p.getAccountNumber()))
                .map(p -> toTransactionalAccount(p, accountNoToBalance.get(p.getAccountNumber())))
                .collect(Collectors.toSet());
    }

    private TransactionalAccount toTransactionalAccount(final Product product, final Amount balance) {
        return TransactionalAccount
                .builder(product.getProductID().getAccountType(), product.getAccountNumber(), balance)
                .setAccountNumber(product.getAccountNumber())
                .addIdentifier(getIban(product.getProductID()))
                .setHolderName(new HolderName(product.getProductID().getAccountOwner().trim()))
                .build();
    }
}
