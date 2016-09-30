
package com.abellimz.sgbusbuzz.models;

import com.google.gson.annotations.SerializedName;

public class Service {

    @SerializedName("ServiceNo")
    private String serviceNo;
    @SerializedName("Status")
    private String status;
    @SerializedName("Operator")
    private String operator;
    @SerializedName("OriginatingID")
    private String originatingID;
    @SerializedName("TerminatingID")
    private String terminatingID;
    @SerializedName("NextBus")
    private Bus nextBus;
    @SerializedName("SubsequentBus")
    private Bus subsequentBus;
    @SerializedName("SubsequentBus3")
    private Bus subsequentBus3;

    @SerializedName("ServiceNo")

    /**
     *
     * @return
     *     The serviceNo
     */
    public String getServiceNo() {
        return serviceNo;
    }

    /**
     * @param serviceNo The ServiceNo
     */
    public void setServiceNo(String serviceNo) {
        this.serviceNo = serviceNo;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The Status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator The Operator
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return The originatingID
     */
    public String getOriginatingID() {
        return originatingID;
    }

    /**
     * @param originatingID The OriginatingID
     */
    public void setOriginatingID(String originatingID) {
        this.originatingID = originatingID;
    }

    /**
     * @return The terminatingID
     */
    public String getTerminatingID() {
        return terminatingID;
    }

    /**
     * @param terminatingID The TerminatingID
     */
    public void setTerminatingID(String terminatingID) {
        this.terminatingID = terminatingID;
    }

    /**
     * @return The nextBus
     */
    public Bus getNextBus() {
        return nextBus;
    }

    /**
     * @param nextBus The Bus
     */
    public void setNextBus(Bus nextBus) {
        this.nextBus = nextBus;
    }

    /**
     * @return The subsequentBus
     */
    public Bus getSubsequentBus() {
        return subsequentBus;
    }

    /**
     * @param subsequentBus The SubsequentBus
     */
    public void setSubsequentBus(Bus subsequentBus) {
        this.subsequentBus = subsequentBus;
    }

    /**
     * @return The subsequentBus3
     */
    public Bus getSubsequentBus3() {
        return subsequentBus3;
    }

    /**
     * @param subsequentBus3 The Bus
     */
    public void setSubsequentBus3(Bus subsequentBus3) {
        this.subsequentBus3 = subsequentBus3;
    }

}
