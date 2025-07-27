package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.dto.web.bybit.WalletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private ObjectMapper objectMapper;

    public WalletResponse getWallet(CredentialsState credentials) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newAccountRestClient();
        var accountRequest = AccountDataRequest.builder().accountType(AccountType.UNIFIED).build();
        var rawResponse = client.getWalletBalance(accountRequest);
        return objectMapper.convertValue(rawResponse, WalletResponse.class);
    }
}
