package com.shubhcrystals.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class EmailProperties {

    private Email email = new Email();
    private String frontendBaseUrl = "http://localhost:3000";

    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }

    public String getFrontendBaseUrl() { return frontendBaseUrl; }
    public void setFrontendBaseUrl(String frontendBaseUrl) { this.frontendBaseUrl = frontendBaseUrl; }

    public static class Email {
        private String fromAddress;
        private String fromName;
        private String adminRecipients = "";

        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }

        public String getAdminRecipients() { return adminRecipients; }
        public void setAdminRecipients(String adminRecipients) { this.adminRecipients = adminRecipients; }

        public List<String> getAdminRecipientList() {
            if (adminRecipients == null || adminRecipients.isBlank()) return List.of();
            return Arrays.stream(adminRecipients.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}
