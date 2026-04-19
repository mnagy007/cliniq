package com.cliniq.external;

import com.cliniq.application.appointment.port.out.CalendarCredentialRepository;
import com.cliniq.domain.appointment.MedicationReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExternalAdapterConfig {

    @Bean
    public RestClient openFdaRestClient(@Value("${openfda.base-url:https://api.fda.gov}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient googleCalendarRestClient(
            @Value("${google.calendar.base-url:https://www.googleapis.com}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public Cache<String, List<MedicationReference>> medicationCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public OpenFdaMedicationAdapter openFdaMedicationAdapter(@Qualifier("openFdaRestClient") RestClient openFdaRestClient,
                                                              Cache<String, List<MedicationReference>> medicationCache,
                                                              ObjectMapper objectMapper) {
        return new OpenFdaMedicationAdapter(openFdaRestClient, medicationCache, objectMapper);
    }

    @Bean
    public GoogleCalendarAdapter googleCalendarAdapter(
            @Qualifier("googleCalendarRestClient") RestClient restClient,
            CalendarCredentialRepository  calendarCredentialRepository,
            ObjectMapper objectMapper) {
        return new GoogleCalendarAdapter(restClient, calendarCredentialRepository, objectMapper);
    }
}