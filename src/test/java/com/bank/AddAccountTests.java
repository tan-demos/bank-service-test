package com.bank;

import com.bank.api.ApiException;
import com.bank.api.model.AddAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class AddAccountTests extends BaseTests {
    private long accountId;

    @BeforeEach
    void setUp() throws ApiException {
        super.setUp();
        accountId = Instant.now().toEpochMilli();
    }

    @Test
    void testAddAccount_NoBalance() throws ApiException {
        var request = new AddAccountRequest().accountId(accountId);
        var account = defaultApi.addAccount(request);
        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals("0", account.getBalance());

    }

    @Test
    void testAddAccount_WithBalance() throws ApiException {
        var request = new AddAccountRequest()
                .accountId(accountId)
                .balance(TestUtil.randomPositiveDecimal().toPlainString());
        var account = defaultApi.addAccount(request);
        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals(request.getBalance(), account.getBalance());
    }

    @Test
    void testAddAccount_InvalidBalance() throws ApiException {
        var request = new AddAccountRequest()
                .accountId(accountId)
                .balance(TestUtil.randomPositiveDecimal().negate().toPlainString());
        assertThrows(ApiException.class, () -> defaultApi.addAccount(request));
    }

    @Test
    void testAddAccount_InvalidAccount() throws ApiException {
        var request = new AddAccountRequest()
                .accountId(-TestUtil.randomPositiveLong());
        assertThrows(ApiException.class, () -> defaultApi.addAccount(request));
    }
}
