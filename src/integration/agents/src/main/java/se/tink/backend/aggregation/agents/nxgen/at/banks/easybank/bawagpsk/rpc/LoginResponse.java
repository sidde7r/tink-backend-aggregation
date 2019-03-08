package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LoginResponse {
    private Envelope envelope;

    public LoginResponse(final Envelope envelope) {
        this.envelope = envelope;
    }

    public boolean requestWasSuccessful() {
        return Optional.ofNullable(envelope.getBody())
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getOk)
                .isPresent();
    }

    public boolean incorrectCredentials() {
        List<String> list =
                Optional.ofNullable(envelope.getBody()).map(Body::getLoginResponseEntity)
                        .map(LoginResponseEntity::getFailure).map(Failure::getResponseMessageList)
                        .orElse(new ArrayList<>()).stream()
                        .map(ResponseMessage::getCode)
                        .collect(Collectors.toList());

        if (!list.isEmpty()) {
            return list.get(0)
                    .trim()
                    .equalsIgnoreCase(BawagPskConstants.MESSAGES.INCORRECT_CREDENTIALS);
        }

        return false;
    }

    public boolean accountIsLocked() {

        List<String> list =
                Optional.ofNullable(envelope.getBody()).map(Body::getLoginResponseEntity)
                        .map(LoginResponseEntity::getFailure).map(Failure::getResponseMessageList)
                        .orElse(new ArrayList<>()).stream()
                        .map(ResponseMessage::getCode)
                        .collect(Collectors.toList());

        if (!list.isEmpty()) {
            return list.get(0) // TODO assert one and only one element
                    .trim()
                    .equalsIgnoreCase(BawagPskConstants.MESSAGES.ACCOUNT_LOCKED);
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
        return envelope.getBody().getLoginResponseEntity().getOk().getDisposer().getProducts()
                .getProductList().stream()
                .filter(product -> product.getAccountNumber().equals(accountNumber))
                .map(Product::getProductID)
                .collect(Collectors.toList())
                .get(0); // TODO assert one and only one element
    }

    public Optional<Products> getProducts() {
        return Optional.ofNullable(envelope)
                .map(Envelope::getBody)
                .map(Body::getLoginResponseEntity)
                .map(LoginResponseEntity::getOk)
                .map(OK::getDisposer)
                .map(Disposer::getProducts);
    }

    /** @return A map from the set of account numbers to the set of product codes */
    public Map<String, String> getProductCodes() {
        final Products products = getProducts().orElseThrow(IllegalStateException::new);
        final List<ProductID> productIDs =
                products.getProductList().stream()
                        .map(Product::getProductID)
                        .filter(productID -> productID.getAccountNumber() != null)
                        .filter(productID -> productID.getProductCode() != null)
                        .collect(Collectors.toList());
        return productIDs.stream()
                .collect(Collectors.toMap(ProductID::getAccountNumber, ProductID::getProductCode));
    }

    /** @return A map from the set of account numbers to the set of product types */
    public Map<String, String> getProductTypes() {
        final Products products = getProducts().orElseThrow(IllegalStateException::new);
        final List<ProductID> productIDs =
                products.getProductList().stream()
                        .map(Product::getProductID)
                        .filter(productID -> productID.getAccountNumber() != null)
                        .filter(productID -> productID.getProductType() != null)
                        .collect(Collectors.toList());
        return productIDs.stream()
                .collect(Collectors.toMap(ProductID::getAccountNumber, ProductID::getProductType));
    }
}
