package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Products")
public class Products {
    private List<Product> productList;

    @XmlElement(name = "Product")
    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public Optional<ProductID> getProductIDByAccountNumber(final String accountNumber) {
        final List<ProductID> productIDs =
                productList.stream()
                        .map(Product::getProductID)
                        .filter(Objects::nonNull)
                        .filter(productID -> productID.getAccountNumber() != null)
                        .filter(
                                productID ->
                                        productID
                                                .getAccountNumber()
                                                .trim()
                                                .equalsIgnoreCase(accountNumber))
                        .collect(Collectors.toList());
        return productIDs.isEmpty() ? Optional.empty() : Optional.of(productIDs.get(0));
    }
}
