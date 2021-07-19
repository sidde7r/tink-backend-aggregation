package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryEntity {
    private String name;
    private String code;
    private String amount;
}
