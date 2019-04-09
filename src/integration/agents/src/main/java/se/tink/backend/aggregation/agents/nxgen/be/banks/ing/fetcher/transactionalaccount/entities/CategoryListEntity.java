package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryListEntity {
    @XmlElement(name = "category")
    private List<CategoryEntity> categoryList;

    public List<CategoryEntity> getCategoryList() {
        return categoryList;
    }

    public void setCategory(List<CategoryEntity> categoryList) {
        this.categoryList = categoryList;
    }
}
