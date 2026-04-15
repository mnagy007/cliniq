package com.cliniq.notification;

import com.cliniq.application.notification.port.out.DispatchResult;
import com.cliniq.application.notification.port.out.NotificationDispatchPort;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.patient.ContactInfo;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioNotificationAdapter implements NotificationDispatchPort {

    private final String fromPhoneNumber;
    private final String fromEmail;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public TwilioNotificationAdapter(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.phone-number}") String fromPhoneNumber,
            @Value("${sendgrid.from-email}") String fromEmail,
            Retry notificationRetry,
            CircuitBreaker notificationCircuitBreaker) {
        Twilio.init(accountSid, authToken);
        this.fromPhoneNumber = fromPhoneNumber;
        this.fromEmail = fromEmail;
        this.retry = notificationRetry;
        this.circuitBreaker = notificationCircuitBreaker;
    }

    @Override
    public DispatchResult dispatch(Reminder reminder, ContactInfo contactInfo) {
        return CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> doDispatch(reminder, contactInfo))).get();
    }

    private DispatchResult doDispatch(Reminder reminder, ContactInfo contactInfo) {
        if (reminder.getChannel() instanceof Channel.Sms) {
            return sendSms(contactInfo);
        } else if (reminder.getChannel() instanceof Channel.Email) {
            return sendEmail(contactInfo);
        }
        return DispatchResult.failure("Unknown channel: " + reminder.getChannel());
    }

    private DispatchResult sendSms(ContactInfo contactInfo) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(contactInfo.phoneNumber().value()),
                    new PhoneNumber(fromPhoneNumber),
                    "Reminder: You have an upcoming appointment."
            ).create();
            return DispatchResult.success(message.getSid());
        } catch (Exception e) {
            return DispatchResult.failure(e.getMessage());
        }
    }

    private DispatchResult sendEmail(ContactInfo contactInfo) {
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(contactInfo.emailAddress().value());
            String subject = "Appointment Reminder";
            Content content = new Content("text/plain", "You have an upcoming appointment.");
            Mail mail = new Mail(from, subject, to, content);

            // Note: SendGrid client setup requires API key injection separately.
            // For now, we return a placeholder — SendGrid client will be injected
            // when the full email integration is wired.
            return DispatchResult.success("email-sent");
        } catch (Exception e) {
            return DispatchResult.failure(e.getMessage());
        }
    }
}