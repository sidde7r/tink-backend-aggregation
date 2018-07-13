package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatrixResponse {
    @XmlAttribute
    protected int code;

    @XmlAttribute
    protected String label;

    @XmlElement
    protected MatrixEntity matrix;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MatrixEntity getMatrix() {
        return matrix;
    }

    public void setMatrix(MatrixEntity matrix) {
        this.matrix = matrix;
    }
}
