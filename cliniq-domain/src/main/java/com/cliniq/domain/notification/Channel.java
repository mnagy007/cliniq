package com.cliniq.domain.notification;

public sealed interface Channel permits Channel.Sms, Channel.Email {

    record Sms() implements Channel {
        public static final Sms INSTANCE = new Sms();
    }

    record Email() implements Channel {
        public static final Email INSTANCE = new Email();
    }
}