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
        acc.setBalance(0.0);                 // ilk bakiye
        return repo.save(acc);
    }

    public List<BankAccount> getAccountsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public void deleteAccount(Long id) {
        repo.deleteById(id);
    }
}
