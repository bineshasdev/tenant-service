package com.offisync360.account.common.constants;

public enum Subscriptions {

    FREE("FREE","Free", 0.0, "Basic access with limited features", 2),
    BASIC("BASIC","Basic", 9.99, "Standard features for individuals",10),
    PRO("PRO","Pro", 29.99, "Advanced features for small teams",100),
    ENTERPRISE("ENTERPRICE", "Enterprise", 99.99, "All features with priority support",20000);

    private final String code;
    private final String displayName;
    private final double pricePerMonth;
    private final String description;
    private final Integer maxUsers;

    Subscriptions(String code, String displayName, double pricePerMonth, String description, Integer maxUsers) {
        this.code = code;
        this.displayName = displayName;
        this.pricePerMonth = pricePerMonth;
        this.description = description;
        this.maxUsers = maxUsers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPricePerMonth() {
        return pricePerMonth;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }
}
