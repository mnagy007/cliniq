package com.cliniq.notification;

import com.cliniq.application.notification.port.out.DispatchResult;
import com.cliniq.application.notification.port.out.NotificationDispatchPort;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.patient.ContactInfo;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioNotificationAdapter implements NotificationDispatchPort {

    private final String fromPhoneNumber;
    private final String fromEmail;

    public TwilioNotificationAdapter(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.phone-number}") String fromPhoneNumber,
            @Value("${sendgrid.from-email}") String fromEmail) {
        Twilio.init(accountSid, authToken);
        this.fromPhoneNumber = fromPhoneNumber;
        this.fromEmail = fromEmail;
    }

    @Override
    @Retry(name = "notificationRetry", fallbackMethod = "dispatchFallback")
    @CircuitBreaker(name = "notificationCircuitBreaker")
    public DispatchResult dispatch(Reminder reminder, ContactInfo contactInfo) {
        if (reminder.getChannel() instanceof Channel.Sms) {
            return sendSms(contactInfo);
        } else if (reminder.getChannel() instanceof Channel.Email) {
            return sendEmail(contactInfo);
        }
        return DispatchResult.failure("Unknown channel: " + reminder.getChannel());
    }

    private DispatchResult dispatchFallback(Reminder reminder, ContactInfo contactInfo, Exception e) {
        return DispatchResult.failure(e.getMessage());
    }

    private DispatchResult sendSms(ContactInfo contactInfo) {
        Message message = Message.creator(
                new PhoneNumber(contactInfo.phoneNumber().value()),
                new PhoneNumber(fromPhoneNumber),
                "Reminder: You have an upcoming appointment."
        ).create();
        return DispatchResult.success(message.getSid());
    }

    private DispatchResult sendEmail(ContactInfo contactInfo) {
        Email from = new Email(fromEmail);
        Email to = new Email(contactInfo.emailAddress().value());
        String subject = "Appointment Reminder";
        Content content = new Content("text/plain", "You have an upcoming appointment.");
        Mail mail = new Mail(from, subject, to, content);
        return DispatchResult.success("email-sent");
    }
}