
package com.allypay.fineract_integration.controller;

import com.allypay.fineract_integration.dto.CreateClientRequest;
import com.allypay.fineract_integration.service.FineractService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


import com.allypay.fineract_integration.dto.SavingsTransactionRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/fineract")
public class FineractController {

    private final FineractService fineractService;

    public FineractController(FineractService fineractService) {
        this.fineractService = fineractService;
    }

    @GetMapping(value = "/offices", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOffices() {
        return fineractService.getOffices();
    }

    @PostMapping(
            value = "/clients",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String createClient(
            @Valid @RequestBody CreateClientRequest request) {
        return fineractService.createClient(request);
    }


    @GetMapping(
        value = "/clients/{clientId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getClient(@PathVariable Long clientId) {
        return fineractService.getClient(clientId);
    }


    @GetMapping("/savings-accounts/{id}")
    public String getSavingsAccount(@PathVariable Long id) {
        return fineractService.getSavingsAccount(id);
    }


    @GetMapping("/savings-accounts/{id}/transactions")
    public String getSavingsTransactions(@PathVariable Long id) {
        return fineractService.getSavingsTransactions(id);
    }


    @PostMapping("/savings-accounts/{id}/deposits")
    public String deposit(
        @PathVariable Long id,
        @Valid @RequestBody SavingsTransactionRequest request
    ) {
        return fineractService.deposit(id, request);
    }

    @PostMapping("/savings-accounts/{id}/withdrawals")
    public String withdraw(
        @PathVariable Long id,
        @Valid @RequestBody SavingsTransactionRequest request
    ) {
        return fineractService.withdraw(id, request);
    }



}
