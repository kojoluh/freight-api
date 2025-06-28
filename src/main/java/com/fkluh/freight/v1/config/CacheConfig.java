package com.fkluh.freight.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.caching.enabled", havingValue = "true", matchIfMissing = false)
public class CacheConfig {}
