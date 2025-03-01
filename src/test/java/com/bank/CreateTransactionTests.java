package com.bank;

import com.bank.api.ApiException;
import com.bank.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class CreateTransactionTests extends BaseTests {
    private Account fromAccount;
    private Account toAccount;

    private BigDecimal fromAccountBalance;

    @BeforeEach
    void setUp() throws ApiException {
        super.setUp();

        fromAccountBalance = TestUtil.randomPositiveDecimal().add(new BigDecimal(100));
        fromAccount = defaultApi.addAccount(new AddAccountRequest()
                .accountId(Instant.now().toEpochMilli())
                .balance(fromAccountBalance.toPlainString()));

        toAccount = defaultApi.addAccount(new AddAccountRequest()
                .accountId(Instant.now().toEpochMilli() + 1));
    }

    @Test
    void createTransaction_HappyCase() throws ApiException {
        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.TRANSFER);
        var transaction = defaultApi.createTransaction(request);
        assertNotNull(transaction);
        assertEquals(TransactionType.TRANSFER.getValue(), transaction.getType());
        assertEquals(TransactionStatus.PENDING.getValue(), transaction.getStatus());
        assertEquals(request.getAmount(), transaction.getAmount());
        assertEquals(request.getFromAccountId(), transaction.getFromAccountId());
        assertEquals(request.getToAccountId(), transaction.getToAccountId());
        assertTrue(transaction.getCreatedAt() > 0);
        assertTrue(transaction.getCompletedAt() == null || transaction.getCompletedAt() == 0);
    }

    @Test
    void createTransaction_InvalidType() {
        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.DEPOSIT);
        assertThrows(ApiException.class, () -> defaultApi.createTransaction(request));
    }

    @Test
    void createTransaction_InvalidFromAccountId() {
        var request = new CreateTransactionRequest()
                .fromAccountId(TestUtil.randomPositiveLong())
                .toAccountId(toAccount.getId())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.TRANSFER);
        assertThrows(ApiException.class, () -> defaultApi.createTransaction(request));
    }

    @Test
    void createTransaction_InvalidToAccountId() {
        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(TestUtil.randomPositiveLong())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.TRANSFER);
        assertThrows(ApiException.class, () -> defaultApi.createTransaction(request));
    }

    @Test
    void createTransaction_TransferToSelfAccount() {
        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(fromAccount.getId())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.TRANSFER);
        assertThrows(ApiException.class, () -> defaultApi.createTransaction(request));
    }

    @Test
    void createTransaction_InSufficientBalance() {
        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(fromAccountBalance.add(new BigDecimal(1)).toPlainString())
                .type(TransactionType.TRANSFER);
        assertThrows(ApiException.class, () -> defaultApi.createTransaction(request));
    }
}
