package com.example.accountmicroservice.service;

import com.example.accountmicroservice.entity.BankAccount;
import com.example.accountmicroservice.entity.Transaction;
import com.example.accountmicroservice.repository.BankAccountRepository;
import com.example.accountmicroservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final BankAccountRepository repo;

    private final TransactionRepository transactionRepo;

    public AccountService(BankAccountRepository repo, TransactionRepository transactionRepo) {
        this.repo = repo;
        this.transactionRepo = transactionRepo;
    }

    public BankAccount createAccount(BankAccount acc) {
        acc.setBalance(0.0);
        if (acc.getAccountNumber() == null || acc.getAccountNumber().isEmpty()) {
            acc.setAccountNumber(generateUniqueAccountNumber());
        }
        return repo.save(acc);
    }

    public List<BankAccount> getAccountsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public BankAccount getAccountById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı: " + id));
    }

    public BankAccount updateBalance(Long id, Double amount) {
        BankAccount account = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı: " + id));

        double newBalance = account.getBalance() + amount;
        if (newBalance < 0) {
            throw new RuntimeException("Yetersiz bakiye: " + account.getBalance());
        }

        account.setBalance(newBalance);
        repo.save(account);

        Transaction tx = new Transaction();
        tx.setAccountId(account.getId());
        tx.setAmount(amount);
        tx.setType(amount > 0 ? "DEPOSIT" : "WITHDRAW");
        transactionRepo.save(tx);

        return account;
    }

    public void transfer(Long fromId, Long toId, Double amount) {
        BankAccount from = repo.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Gönderen hesap bulunamadı: " + fromId));
        BankAccount to = repo.findById(toId)
                .orElseThrow(() -> new RuntimeException("Alıcı hesap bulunamadı: " + toId));

        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new RuntimeException("Para birimleri uyumsuz! Gönderen: "
                    + from.getCurrency() + ", Alıcı: " + to.getCurrency());
        }

        if (from.getBalance() < amount) {
            throw new RuntimeException("Yetersiz bakiye: " + from.getBalance());
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        repo.save(from);
        repo.save(to);

        // Transfer kayıtları
        Transaction outTx = new Transaction();
        outTx.setAccountId(from.getId());
        outTx.setAmount(-amount);
        outTx.setType("TRANSFER_OUT");
        outTx.setTargetAccountId(to.getId());
        transactionRepo.save(outTx);

        Transaction inTx = new Transaction();
        inTx.setAccountId(to.getId());
        inTx.setAmount(amount);
        inTx.setType("TRANSFER_IN");
        inTx.setTargetAccountId(from.getId());
        transactionRepo.save(inTx);
    }



    public void deleteAccount(Long id) {
        repo.deleteById(id);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "TR" + (long)(Math.random() * 1_000_000_000_000_000L);
        } while (repo.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    // TRANSACTIONS

    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepo.findByAccountId(accountId);
    }
}
