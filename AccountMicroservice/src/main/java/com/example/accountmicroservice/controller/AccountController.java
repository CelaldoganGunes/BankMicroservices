package com.example.accountmicroservice.controller;

import com.example.accountmicroservice.dto.BalanceRequest;
import com.example.accountmicroservice.entity.BankAccount;
import com.example.accountmicroservice.entity.Transaction;
import com.example.accountmicroservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccountById(id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTransactions(id));
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<BankAccount> updateBalance(
            @PathVariable Long id,
            @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(service.updateBalance(id, request.getAmount()));
    }

    @PostMapping("/create")
    public ResponseEntity<BankAccount> create(@RequestBody BankAccount acc) {
        return ResponseEntity.ok(service.createAccount(acc));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BankAccount>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getAccountsByUser(userId));
    }

    @PostMapping("/from/{fromId}/to/{toId}")
    public ResponseEntity<String> transfer(
            @PathVariable Long fromId,
            @PathVariable Long toId,
            @RequestBody BalanceRequest request) {
        service.transfer(fromId, toId, request.getAmount());
        return ResponseEntity.ok("Transfer başarılı");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/celal")
    public ResponseEntity<String> byCelal() {
        return ResponseEntity.ok("celal başkan");
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetDatabase() {
        service.resetDatabase();
        return ResponseEntity.ok("Veritabanı sıfırlandı");
    }

}
