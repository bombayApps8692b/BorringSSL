package com.connection;

import org.json.JSONObject;

public enum ResponseType {
    HAND_SHAKE,SCRP_HAND_SHAKE, ERROR, IDK, IB, UB, CP, AD, AB, VB, AF, DA, DNA, DNR, DR, UROD, SUCCESS,SI, UNKNOWN;

    private boolean isFailure;
    private String message="";
    private JSONObject jsonObject;

    public static ResponseType getResponseType(String response) {
        ResponseType responseType = null;
        try {
            responseType = ResponseType.valueOf(response.toUpperCase());
        } catch (IllegalArgumentException iae) {
            responseType = ResponseType.UNKNOWN;
        }

        return responseType;
    }

    public void setFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
