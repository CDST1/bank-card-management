package com.bank.bank_app.service;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.exception.CardNotFoundException;
import com.bank.bank_app.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private EncryptionService encryptionService;

    public Page<Card> getUserCardsWithFilters(User user, String ownerName, CardStatus status, Pageable pageable) {
        if (ownerName != null && status != null) {
            return cardRepository.findByUserAndOwnerNameContainingIgnoreCaseAndStatus(user, ownerName, status, pageable)
                    .map(this::maskCardNumber);
        } else if (ownerName != null) {
            return cardRepository.findByUserAndOwnerNameContainingIgnoreCase(user, ownerName, pageable)
                    .map(this::maskCardNumber);
        } else if (status != null) {
            return cardRepository.findByUserAndStatus(user, status, pageable)
                    .map(this::maskCardNumber);
        } else {
            return cardRepository.findByUser(user, pageable)
                    .map(this::maskCardNumber);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredCards() {
        List<Card> allCards = cardRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Card card : allCards) {
            if (card.getExpiryDate().isBefore(today) && card.getStatus() != CardStatus.EXPIRED) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
                System.out.println("Card " + card.getId() + " marked as EXPIRED");
            }
        }
    }

    public Card createCard(Card card) {
        String encryptedCardNumber = encryptionService.encrypt(card.getCardNumber());
        card.setCardNumber(encryptedCardNumber);

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
        }

        return cardRepository.save(card);
    }

    public Card getCardWithMaskedNumber(Long id) {
        Card card = getCardById(id);
        return maskCardNumber(card);
    }

    public Card maskCardNumber(Card card) {
        try {
            System.out.println("Masking card ID: " + card.getId());
            System.out.println("Raw card number from DB: " + card.getCardNumber());

            String decryptedNumber = encryptionService.decrypt(card.getCardNumber());
            System.out.println("Decrypted number: " + decryptedNumber);

            String maskedNumber = "**** **** **** " + decryptedNumber.substring(12);
            System.out.println("Masked number: " + maskedNumber);

            card.setCardNumber(maskedNumber);
            return card;

        } catch (Exception e) {
            System.out.println("Error masking card: " + e.getMessage());
            e.printStackTrace();
            card.setCardNumber("**** **** **** ****");
            return card;
        }
    }

    public Page<Card> getUserCards(User user, Pageable pageable) {
        Page<Card> cards = cardRepository.findByUser(user, pageable);
        return cards.map(this::maskCardNumber);
    }

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
    }

    public Card updateBalance(Long id, BigDecimal newBalance) {
        Card card = getCardById(id);
        card.setBalance(newBalance);
        return cardRepository.save(card);
    }

    public Card blockCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public Card activateCard(Long id) {
        Card card = getCardById(id);
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public void deleteCard(Long id) {
        Card card = getCardById(id);
        cardRepository.delete(card);
    }

    public Optional<Card> findByCardNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber);
    }
}