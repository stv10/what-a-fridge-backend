package com.stv10.whatafridge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openfoodfacts")
public class OpenFoodFactsProperties {
    private String baseUrl = "https://world.openfoodfacts.org";
    private String contactEmail;
    private String appName = "WhatAFridge";
    private String appVersion = "1.0";
}
