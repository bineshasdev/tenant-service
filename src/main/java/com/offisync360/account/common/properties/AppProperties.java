package com.offisync360.account.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private ClientIds clientIds = new ClientIds();

    // Getters and Setters
    public ClientIds getClientIds() {
        return clientIds;
    }

    public void setClientIds(ClientIds clientIds) {
        this.clientIds = clientIds;
    }
}
