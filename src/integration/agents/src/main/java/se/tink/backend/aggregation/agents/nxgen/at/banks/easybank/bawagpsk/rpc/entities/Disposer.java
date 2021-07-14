package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Disposer")
public class Disposer {
    private Products products;

    @XmlElement(name = "Products")
    public void setProducts(Products products) {
        this.products = products;
    }

    public Products getProducts() {
        return products;
    }
}
