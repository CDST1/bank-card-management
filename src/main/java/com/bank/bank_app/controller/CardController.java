package com.bank.bank_app.controller;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.service.CardService;
import com.bank.bank_app.service.SecurityService;
import com.bank.bank_app.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Карты", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private SecurityService securityService;

    @GetMapping
    @Operation(summary = "Получить карты пользователя",
            description = "Возвращает список карт с пагинацией (страницы начинаются с 1) и фильтрацией")
    public ResponseEntity<Page<Card>> getUserCards(
            Authentication authentication,
            @Parameter(description = "Номер страницы (начинается с 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Фильтр по имени владельца")
            @RequestParam(required = false) String ownerName,
            @Parameter(description = "Фильтр по статусу карты")
            @RequestParam(required = false) CardStatus status) {

        User user = securityService.getCurrentUser(authentication);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Card> cards = cardService.getUserCardsWithFilters(user, ownerName, status, pageable);

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить карту по ID",
            description = "Возвращает информацию о конкретной карте с маскированным номером")
    public ResponseEntity<Card> getCard(@PathVariable Long id, Authentication authentication) {
        User user = securityService.getCurrentUser(authentication);
        Card card = cardService.getCardWithMaskedNumber(id);

        if (!card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(card);
    }

    @PutMapping("/{id}/request-block")
    @Operation(summary = "Запрос на блокировку карты",
            description = "Пользователь запрашивает блокировку своей карты")
    public ResponseEntity<Card> requestBlockCard(@PathVariable Long id, Authentication authentication) {
        User user = securityService.getCurrentUser(authentication);
        Card card = cardService.getCardWithMaskedNumber(id);

        if (!card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        Card blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(cardService.maskCardNumber(blockedCard));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами",
            description = "Перевод средств между картами одного пользователя")
    public ResponseEntity<?> transferBetweenCards(
            Authentication authentication,
            @RequestBody TransferRequest request) {

        try {
            User user = securityService.getCurrentUser(authentication);
            transferService.transferBetweenOwnCards(user, request.getFromCardId(),
                    request.getToCardId(), request.getAmount());

            return ResponseEntity.ok("Transfer successful");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class TransferRequest {
        private Long fromCardId;
        private Long toCardId;
        private BigDecimal amount;

        public Long getFromCardId() {
            return fromCardId;
        }

        public void setFromCardId(Long fromCardId) {
            this.fromCardId = fromCardId;
        }

        public Long getToCardId() {
            return toCardId;
        }

        public void setToCardId(Long toCardId) {
            this.toCardId = toCardId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}