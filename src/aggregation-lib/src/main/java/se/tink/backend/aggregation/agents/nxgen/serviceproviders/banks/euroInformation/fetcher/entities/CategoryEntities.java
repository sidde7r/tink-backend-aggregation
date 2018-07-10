package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "category_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryEntities {
    private List<CategoryEntity> categoryList;
}
