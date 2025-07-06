package com.example.accountmicroservice.service;

import com.example.accountmicroservice.entity.BankAccount;
import com.example.accountmicroservice.entity.Transaction;
import com.example.accountmicroservice.repository.BankAccountRepository;
import com.example.accountmicroservice.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final BankAccountRepository repo;

    private final TransactionRepository transactionRepo;

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final String notificationUrl = "http://localhost:8083/api/notifications/send";

    public AccountService(BankAccountRepository repo, TransactionRepository transactionRepo, JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        this.repo = repo;
        this.transactionRepo = transactionRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
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
        sendNotification(account.getUserId(), account.getId(), amount);
        return account;
    }

    public void transfer(Long fromId, Long toId, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Transfer miktarı pozitif olmalıdır. Girilen: " + amount);
        }

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

        // Transfer hareketleri
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
        sendNotification(from.getUserId(), from.getId(), -amount);
        sendNotification(to.getUserId(), to.getId(), amount);
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

    public void resetDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE transaction");
        jdbcTemplate.execute("TRUNCATE TABLE bank_account");
    }

    private void sendNotification(Long userId, Long accountId, Double amount) {
        String email = "user"+userId+"@mail.com"; // Simülasyon
        String message = "Hesabınız ("+accountId+") için işlem gerçekleşti: " + amount;

        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("message", message);

        ResponseEntity<String> response = restTemplate.postForEntity(
                notificationUrl,
                payload,
                String.class);

        System.out.println("Notification gönderildi: " + response.getBody());
    }
}
