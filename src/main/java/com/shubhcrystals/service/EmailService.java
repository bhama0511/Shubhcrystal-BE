package com.shubhcrystals.service;

import com.shubhcrystals.config.EmailProperties;
import com.shubhcrystals.dto.OrderResponse;
import com.shubhcrystals.model.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties props;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine,
                        EmailProperties props) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.props = props;
    }

    @Async("emailTaskExecutor")
    public void sendWelcome(User user) {
        Context ctx = baseContext();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("shopUrl", props.getFrontendBaseUrl() + "/shop");
        send(user.getEmail(), "Welcome to ShubhCrystals 💎", "email/welcome", ctx);
    }

    @Async("emailTaskExecutor")
    public void sendPasswordReset(User user, String token) {
        Context ctx = baseContext();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("resetUrl", props.getFrontendBaseUrl() + "/reset-password?token=" + token);
        ctx.setVariable("expiryMinutes", 30);
        send(user.getEmail(), "Reset your ShubhCrystals password", "email/password-reset", ctx);
    }

    @Async("emailTaskExecutor")
    public void sendOrderPlaced(OrderResponse order, User user) {
        Context ctx = baseContext();
        ctx.setVariable("order", order);
        ctx.setVariable("name", user.getName());
        ctx.setVariable("ordersUrl", props.getFrontendBaseUrl() + "/orders");
        send(user.getEmail(),
                "Order #" + order.id() + " received — thank you!",
                "email/order-placed", ctx);
    }

    @Async("emailTaskExecutor")
    public void sendOrderStatusUpdate(OrderResponse order, User user, String oldStatus) {
        Context ctx = baseContext();
        ctx.setVariable("order", order);
        ctx.setVariable("name", user.getName());
        ctx.setVariable("oldStatus", oldStatus);
        ctx.setVariable("ordersUrl", props.getFrontendBaseUrl() + "/orders");
        send(user.getEmail(),
                "Order #" + order.id() + " is now " + prettyStatus(order.status()),
                "email/order-status", ctx);
    }

    @Async("emailTaskExecutor")
    public void sendAdminNewOrder(OrderResponse order, User user) {
        List<String> admins = props.getEmail().getAdminRecipientList();
        if (admins.isEmpty()) {
            log.info("No admin recipients configured — skipping admin alert for order {}", order.id());
            return;
        }
        Context ctx = baseContext();
        ctx.setVariable("order", order);
        ctx.setVariable("customerName", user.getName());
        ctx.setVariable("customerEmail", user.getEmail());
        ctx.setVariable("adminUrl", props.getFrontendBaseUrl() + "/admin/orders");
        sendMulti(admins,
                "New order #" + order.id() + " — ₹" + order.total(),
                "email/admin-new-order", ctx);
    }

    private Context baseContext() {
        Context ctx = new Context();
        ctx.setVariable("brandName", props.getEmail().getFromName());
        return ctx;
    }

    private void send(String to, String subject, String templateName, Context ctx) {
        sendMulti(List.of(to), subject, templateName, ctx);
    }

    private void sendMulti(List<String> to, String subject, String templateName, Context ctx) {
        try {
            String html = templateEngine.process(templateName, ctx);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    props.getEmail().getFromAddress(),
                    props.getEmail().getFromName(),
                    StandardCharsets.UTF_8.name()));
            helper.setTo(to.toArray(String[]::new));
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Sent email '{}' to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }

    private String prettyStatus(String status) {
        if (status == null) return "";
        return status.charAt(0) + status.substring(1).toLowerCase();
    }
}
