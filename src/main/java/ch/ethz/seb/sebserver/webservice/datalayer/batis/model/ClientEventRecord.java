package ch.ethz.seb.sebserver.webservice.datalayer.batis.model;

import java.math.BigDecimal;
import javax.annotation.Generated;

public class ClientEventRecord {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.id")
    private Long id;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.connection_id")
    private Long connectionId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.user_identifier")
    private String userIdentifier;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.type")
    private Integer type;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.timestamp")
    private Long timestamp;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.numeric_value")
    private BigDecimal numericValue;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.870+01:00", comments="Source field: client_event.text")
    private String text;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source Table: client_event")
    public ClientEventRecord(Long id, Long connectionId, String userIdentifier, Integer type, Long timestamp, BigDecimal numericValue, String text) {
        this.id = id;
        this.connectionId = connectionId;
        this.userIdentifier = userIdentifier;
        this.type = type;
        this.timestamp = timestamp;
        this.numericValue = numericValue;
        this.text = text;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.id")
    public Long getId() {
        return id;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.connection_id")
    public Long getConnectionId() {
        return connectionId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.user_identifier")
    public String getUserIdentifier() {
        return userIdentifier;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.type")
    public Integer getType() {
        return type;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.869+01:00", comments="Source field: client_event.numeric_value")
    public BigDecimal getNumericValue() {
        return numericValue;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2019-01-15T10:10:06.870+01:00", comments="Source field: client_event.text")
    public String getText() {
        return text;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table client_event
     *
     * @mbg.generated Tue Jan 15 10:10:06 CET 2019
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", connectionId=").append(connectionId);
        sb.append(", userIdentifier=").append(userIdentifier);
        sb.append(", type=").append(type);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", numericValue=").append(numericValue);
        sb.append(", text=").append(text);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table client_event
     *
     * @mbg.generated Tue Jan 15 10:10:06 CET 2019
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ClientEventRecord other = (ClientEventRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getConnectionId() == null ? other.getConnectionId() == null : this.getConnectionId().equals(other.getConnectionId()))
            && (this.getUserIdentifier() == null ? other.getUserIdentifier() == null : this.getUserIdentifier().equals(other.getUserIdentifier()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getTimestamp() == null ? other.getTimestamp() == null : this.getTimestamp().equals(other.getTimestamp()))
            && (this.getNumericValue() == null ? other.getNumericValue() == null : this.getNumericValue().equals(other.getNumericValue()))
            && (this.getText() == null ? other.getText() == null : this.getText().equals(other.getText()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table client_event
     *
     * @mbg.generated Tue Jan 15 10:10:06 CET 2019
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getConnectionId() == null) ? 0 : getConnectionId().hashCode());
        result = prime * result + ((getUserIdentifier() == null) ? 0 : getUserIdentifier().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getTimestamp() == null) ? 0 : getTimestamp().hashCode());
        result = prime * result + ((getNumericValue() == null) ? 0 : getNumericValue().hashCode());
        result = prime * result + ((getText() == null) ? 0 : getText().hashCode());
        return result;
    }
}