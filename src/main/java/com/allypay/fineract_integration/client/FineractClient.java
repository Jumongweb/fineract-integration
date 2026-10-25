
package com.allypay.fineract_integration.client;

import com.allypay.fineract_integration.dto.CreateClientRequest;
import com.allypay.fineract_integration.dto.SavingsTransactionRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;



@Component
public class FineractClient {

    private final RestClient restClient;

    public FineractClient(
            RestClient.Builder builder,
            @Value("${fineract.base-url}") String baseUrl,
            @Value("${fineract.tenant-id}") String tenantId,
            @Value("${fineract.username}") String username,
            @Value("${fineract.password}") String password) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Fineract-Platform-TenantId", tenantId)
                .defaultHeaders(headers ->
                        headers.setBasicAuth(username, password))
                .build();
    }

    public String getOffices() {
        return restClient.get()
                .uri("/offices")
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(String.class);
    }

    public String createClient(CreateClientRequest request) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("officeId", request.officeId());
        payload.put("firstname", request.firstname());
        payload.put("lastname", request.lastname());

        if (request.externalId() != null
                && !request.externalId().isBlank()) {
            payload.put("externalId", request.externalId());
        }

        payload.put("legalFormId", 1);
        payload.put("active", true);
        payload.put(
                "activationDate",
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd MMMM yyyy",
                                java.util.Locale.ENGLISH))
        );
        payload.put("dateFormat", "dd MMMM yyyy");
        payload.put("locale", "en");

        return restClient.post()
                .uri("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }


    public String getClient(Long clientId) {
        return restClient.get()
                .uri("/clients/{clientId}", clientId)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(String.class);
    }


    public String getSavingsAccount(Long savingsAccountId) {
        return restClient.get()
                .uri("/savingsaccounts/{id}", savingsAccountId)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(String.class);
    }


        public String getSavingsTransactions(Long savingsAccountId) {
                return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/savingsaccounts/{id}")
                    .queryParam("associations", "transactions")
                    .build(savingsAccountId))
                        .header(HttpHeaders.ACCEPT, "application/json")
                        .retrieve()
                        .body(String.class);
        }


public String createSavingsTransaction(
        Long savingsAccountId,
        SavingsTransactionRequest request,
        String command
) {
    if (!"deposit".equals(command)
            && !"withdrawal".equals(command)) {
        throw new IllegalArgumentException(
                "Unsupported transaction command"
        );
    }

    String transactionDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy",
                    Locale.ENGLISH
            ));

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("transactionDate", transactionDate);
    payload.put("transactionAmount", request.amount());
    payload.put("paymentTypeId", request.paymentTypeId());
    payload.put("dateFormat", "dd MMMM yyyy");
    payload.put("locale", "en");

    return restClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path("/savingsaccounts/{id}/transactions")
                    .queryParam("command", command)
                    .build(savingsAccountId))
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body(String.class);
}






}
