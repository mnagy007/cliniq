package com.cliniq.web.security;

import com.cliniq.shared.domain.TenantId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Extracts the tenant ID from the JWT Authorization header and sets it in TenantContext.
 * 
 * Note: This filter parses the JWT payload without signature verification.
 * In production, JWT verification should be performed by a proper security layer.
 */
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                TenantId tenantId = extractTenantIdFromJwt(token);
                if (tenantId != null) {
                    TenantContext.set(tenantId);
                }
            }
            
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Extracts the tenantId from the JWT payload without signature verification.
     * 
     * JWT format: header.payload.signature (three base64url-encoded parts separated by dots)
     * 
     * @param token the JWT token string
     * @return TenantId if found in payload, null otherwise
     */
    private TenantId extractTenantIdFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // Decode the payload (second part)
            String payloadJson = new String(DECODER.decode(parts[1]));
            
            // Extract tenantId from JSON payload
            String tenantId = extractJsonValue(payloadJson, "tenantId");
            if (tenantId != null && !tenantId.isEmpty()) {
                return new TenantId(UUID.fromString(tenantId));
            }
            
            return null;
        } catch (Exception e) {
            // If JWT parsing fails, return null (no tenant context will be set)
            return null;
        }
    }

    /**
     * Simple JSON value extraction without a full JSON parser.
     * Looks for "tenantId":"value" pattern in the JSON string.
     */
    private String extractJsonValue(String json, String key) {
        // Pattern: "key":"value" or "key": "value"
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        // Find the colon after the key
        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) {
            return null;
        }
        
        // Find the opening quote of the value
        int valueStart = json.indexOf('"', colonIndex + 1);
        if (valueStart == -1) {
            return null;
        }
        
        // Find the closing quote of the value
        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd == -1) {
            return null;
        }
        
        return json.substring(valueStart + 1, valueEnd);
    }
}
