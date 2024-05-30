package com.shy.custum;

// 单例类
public class StatusManager {
    private static StatusManager instance;
    private String statusText;

    private StatusManager() {}

    public static synchronized StatusManager getInstance() {
        if (instance == null) {
            instance = new StatusManager();
        }
        return instance;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
