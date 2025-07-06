package com.example.accountmicroservice.service;

import com.example.accountmicroservice.entity.BankAccount;
import com.example.accountmicroservice.repository.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final BankAccountRepository repo;

    public AccountService(BankAccountRepository repo) {
        this.repo = repo;
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
        return repo.save(account);
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
}
