package com.example.yin2.domain;

import java.util.List;

public class SignInRes {
    private String accessToken;

    private List<Consumer> consumerList;

    public SignInRes() {
    }

    public SignInRes(String accessToken, List<Consumer> consumerList) {
        this.accessToken = accessToken;
        this.consumerList = consumerList;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Consumer> getConsumerList() {
        return consumerList;
    }

    public void setConsumerList(List<Consumer> consumerList) {
        this.consumerList = consumerList;
    }

    @Override
    public String toString() {
        return "signInRes{" +
                "accessToken='" + accessToken + '\'' +
                ", consumerList=" + consumerList +
                '}';
    }
}
