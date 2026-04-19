package com.cliniq.web.patient.dto;

public record UpdateContactInfoRequest(
    String phoneNumber,
    String emailAddress
) {}
