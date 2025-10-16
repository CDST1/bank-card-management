package com.bank.bank_app.controller;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.repository.UserRepository;
import com.bank.bank_app.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Администрирование", description = "API для административных функций")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/cards")
    @Operation(summary = "Создать карту", description = "Создает новую банковскую карту для пользователя с указанным балансом")
    public ResponseEntity<Card> createCard(@RequestBody CreateCardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        String cardNumber = generateCardNumber();

        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setOwnerName(request.getOwnerName());
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        Card savedCard = cardService.createCard(card);
        return ResponseEntity.ok(savedCard);
    }

    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Удалить карту", description = "Удаляет карту по ID")
    public ResponseEntity<?> deleteCard(@PathVariable Long cardId) {
        cardService.getCardById(cardId);
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card deleted successfully");
    }

    @PutMapping("/cards/{cardId}/block")
    @Operation(summary = "Блокировать карту", description = "Административная блокировка карты")
    public ResponseEntity<Card> blockCard(@PathVariable Long cardId) {
        Card card = cardService.blockCard(cardId);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/cards/{cardId}/activate")
    @Operation(summary = "Активировать карту", description = "Активация заблокированной карты")
    public ResponseEntity<Card> activateCard(@PathVariable Long cardId) {
        Card card = cardService.activateCard(cardId);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/cards/{cardId}/balance")
    @Operation(summary = "Пополнить баланс карты", description = "Административное пополнение баланса карты")
    public ResponseEntity<Card> updateBalance(
            @PathVariable Long cardId,
            @RequestParam BigDecimal amount) {

        Card card = cardService.getCardById(cardId);
        BigDecimal newBalance = card.getBalance().add(amount);
        Card updatedCard = cardService.updateBalance(cardId, newBalance);

        return ResponseEntity.ok(updatedCard);
    }

    @PostMapping("/test-data")
    @Operation(summary = "Создать тестовые данные", description = "Создает тестовые карты для пользователя")
    public ResponseEntity<?> createTestData(@RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Card card1 = new Card();
            card1.setCardNumber(generateCardNumber());
            card1.setOwnerName("Test User");
            card1.setExpiryDate(LocalDate.now().plusYears(3));
            card1.setBalance(new BigDecimal("5000.00"));
            card1.setStatus(CardStatus.ACTIVE);
            card1.setUser(user);
            cardService.createCard(card1);

            Card card2 = new Card();
            card2.setCardNumber(generateCardNumber());
            card2.setOwnerName("Test User");
            card2.setExpiryDate(LocalDate.now().plusYears(3));
            card2.setBalance(new BigDecimal("3000.00"));
            card2.setStatus(CardStatus.ACTIVE);
            card2.setUser(user);
            cardService.createCard(card2);

            return ResponseEntity.ok("Test cards created successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating test data: " + e.getMessage());
        }
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    public static class CreateCardRequest {
        private Long userId;
        private String ownerName;
        private BigDecimal balance;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
    }
}