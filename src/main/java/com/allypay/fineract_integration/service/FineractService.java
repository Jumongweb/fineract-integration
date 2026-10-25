
package com.allypay.fineract_integration.service;

import com.allypay.fineract_integration.client.FineractClient;
import com.allypay.fineract_integration.dto.CreateClientRequest;
import org.springframework.stereotype.Service;
import com.allypay.fineract_integration.dto.SavingsTransactionRequest;


@Service
public class FineractService {

    private final FineractClient fineractClient;

    public FineractService(FineractClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public String getOffices() {
        return fineractClient.getOffices();
    }

    public String createClient(CreateClientRequest request) {
        return fineractClient.createClient(request);
    }


    public String getClient(Long clientId) {
        return fineractClient.getClient(clientId);
    }

    
    public String getSavingsAccount(Long savingsAccountId) {
        return fineractClient.getSavingsAccount(savingsAccountId);
    }

    
    public String getSavingsTransactions(Long savingsAccountId) {
        return fineractClient.getSavingsTransactions(savingsAccountId);
    }


public String deposit(
        Long savingsAccountId,
        SavingsTransactionRequest request
) {
    return fineractClient.createSavingsTransaction(
            savingsAccountId, request, "deposit"
    );
}

public String withdraw(
        Long savingsAccountId,
        SavingsTransactionRequest request
) {
    return fineractClient.createSavingsTransaction(
            savingsAccountId, request, "withdrawal"
    );
}





}
