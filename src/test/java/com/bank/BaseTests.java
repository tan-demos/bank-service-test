package com.bank;

import com.bank.api.ApiException;
import com.bank.api.client.DefaultApi;
import org.junit.jupiter.api.BeforeEach;

public class BaseTests {
    DefaultApi defaultApi;

    @BeforeEach
    void setUp() throws ApiException {
        defaultApi = new DefaultApi();
        // base url should point to bank-service alpha cluster address
        defaultApi.setCustomBaseUrl("http://localhost:8080");
    }
}
