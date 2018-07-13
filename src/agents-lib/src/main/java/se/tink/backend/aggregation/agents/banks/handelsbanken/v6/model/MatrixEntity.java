package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MatrixEntity {
    @XmlElement
    protected int matrixId;

    @XmlElement
    protected String matrixMetaData;

    @XmlElement
    protected String image;

    public int getMatrixId() {
        return matrixId;
    }

    public void setMatrixId(int matrixId) {
        this.matrixId = matrixId;
    }

    public String getMatrixMetaData() {
        return matrixMetaData;
    }

    public void setMatrixMetaData(String matrixMetaData) {
        this.matrixMetaData = matrixMetaData;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
