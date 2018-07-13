package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Failure;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Product;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ResponseMessage;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class LoginResponse {
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
        return envelope.getBody()
                .getLoginResponseEntity().getOk().getDisposer().getProducts().getProductList().stream()
                .map(Product::getProductID)
                .collect(Collectors.toList());
    }

    public List<Product> getProductList() {
        return envelope.getBody().getLoginResponseEntity().getOk().getDisposer().getProducts().getProductList();
    }

    public Collection<IbanIdentifier> getInvalidIbans() {
        return getProductList().stream()
                .map(Product::getIban)
                .filter(iban -> !iban.isValid())
                .collect(Collectors.toList());
    }
}
