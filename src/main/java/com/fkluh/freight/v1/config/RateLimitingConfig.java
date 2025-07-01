package com.fkluh.freight.v1.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RateLimitingConfig {

    @Bean
    public FilterRegistrationBean<Filter> rateLimitingFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/api/v1/*"); // Apply to specific endpoints
        registrationBean.setOrder(1); // Set filter order
        return registrationBean;
    }

    private static class RateLimitingFilter implements Filter {

        private final Bucket bucket;

        public RateLimitingFilter() {
            // Allow 100 requests per minute
            Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
            this.bucket = Bucket.builder().addLimit(limit).build();
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(429); // 429 Too Many Requests
                httpResponse.getWriter().write("Too many requests. Please try again later.");
            }
        }
    }
}