package com.bank;

import com.bank.api.ApiException;
import com.bank.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitTransactionTests extends BaseTests {
    private Transaction transaction;
    private Account fromAccount;
    private Account toAccount;
    private BigDecimal fromAccountBalance;
    private BigDecimal amount;

    @BeforeEach
    void setUp() throws ApiException {
        super.setUp();

        fromAccountBalance = TestUtil.randomPositiveDecimal().add(new BigDecimal(100));
        fromAccount = defaultApi.addAccount(new AddAccountRequest()
                .accountId(Instant.now().toEpochMilli())
                .balance(fromAccountBalance.toPlainString()));

        toAccount = defaultApi.addAccount(new AddAccountRequest()
                .accountId(Instant.now().toEpochMilli()+1));

        amount = fromAccountBalance.subtract(new BigDecimal(TestUtil.randomPositiveLong() % 100));

        var request = new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(amount.toPlainString())
                .type(TransactionType.TRANSFER);
        transaction = defaultApi.createTransaction(request);
    }

    @Test
    void submitTransaction_HappyCase() throws ApiException {
        var result = defaultApi.submitTransaction(transaction.getId());
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED.getValue(), result.getStatus());

        var balance = new BigDecimal(defaultApi.getAccountById(fromAccount.getId()).getBalance());
        assertEquals(fromAccountBalance.subtract(amount), balance);
        balance = new BigDecimal(defaultApi.getAccountById(toAccount.getId()).getBalance());
        assertEquals(amount, balance);

        var got = defaultApi.getTransactionById(transaction.getId());
        assertEquals(result, got);
    }

    @Test
    void submitTransaction_DuplicateSubmission() throws ApiException {
        var result = defaultApi.submitTransaction(transaction.getId());
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED.getValue(), result.getStatus());
        result = defaultApi.submitTransaction(transaction.getId());
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED.getValue(), result.getStatus());
        var balance = new BigDecimal(defaultApi.getAccountById(fromAccount.getId()).getBalance());
        assertEquals(fromAccountBalance.subtract(amount), balance);
        balance = new BigDecimal(defaultApi.getAccountById(toAccount.getId()).getBalance());
        assertEquals(amount, balance);

    }

    @Test
    void submitTransaction_InsufficientBalanceCausedByConcurrentTransaction() throws ApiException {

        var transaction1 = defaultApi.createTransaction(new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(amount.toPlainString())
                .type(TransactionType.TRANSFER));

        var transaction2 = defaultApi.createTransaction(new CreateTransactionRequest()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(fromAccountBalance.toPlainString())
                .type(TransactionType.TRANSFER));

        var result2 = defaultApi.submitTransaction(transaction2.getId());
        assertNotNull(result2);
        assertEquals(TransactionStatus.SUCCEEDED.getValue(), result2.getStatus());

        // Insufficient balance for previous transaction 1
        assertThrows(ApiException.class, () -> defaultApi.submitTransaction(transaction1.getId()));
    }
}
