package com.cliniq.external;

import com.cliniq.application.medication.port.out.MedicationLookupPort;
import com.cliniq.domain.appointment.MedicationReference;
import com.cliniq.shared.domain.TenantId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;

public class OpenFdaMedicationAdapter implements MedicationLookupPort {

    private static final Logger log = LoggerFactory.getLogger(OpenFdaMedicationAdapter.class);

    private final RestClient restClient;
    private final Cache<String, List<MedicationReference>> cache;
    private final ObjectMapper objectMapper;

    public OpenFdaMedicationAdapter(RestClient restClient,
                                     Cache<String, List<MedicationReference>> cache,
                                     ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MedicationReference> search(String query, TenantId tenantId) {
        String cacheKey = tenantId.value() + ":" + query;
        return cache.get(cacheKey, key -> fetchFromFda(query));
    }

    List<MedicationReference> fetchFromFda(String query) {
        try {
            var response = restClient.get()
                    .uri("/drug/ndc.json?search=brand_name:{query}&limit=20", query)
                    .retrieve()
                    .body(String.class);

            if (response == null) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            if (results == null || !results.isArray()) {
                return List.of();
            }

            List<MedicationReference> medications = new ArrayList<>();
            for (JsonNode result : results) {
                String ndcCode = getTextOrDefault(result, "product_ndc", "");
                String brandName = getTextOrDefault(result, "brand_name", "");
                String genericName = getTextOrDefault(result, "generic_name", "");

                if (!ndcCode.isBlank()) {
                    medications.add(new MedicationReference(ndcCode, brandName, genericName));
                }
            }
            return medications;
        } catch (Exception e) {
            log.error("Error fetching medications from OpenFDA for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : defaultValue;
    }
}