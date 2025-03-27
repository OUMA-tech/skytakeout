package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.stripe")
@Data
public class StripeProperties {

    /**
     * Stripe secret key
     */
    private String secretKey;

}