package com.bank.bank_app.service;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.exception.InsufficientFundsException;
import com.bank.bank_app.exception.TransferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    @Autowired
    private CardService cardService;

    @Transactional
    public void transferBetweenOwnCards(User user, Long fromCardId, Long toCardId, BigDecimal amount) {

        if (fromCardId.equals(toCardId)) {
            throw new TransferException("Cannot transfer to the same card");
        }

        Card fromCard = cardService.getCardById(fromCardId);
        Card toCard = cardService.getCardById(toCardId);

        if (!fromCard.getUser().getId().equals(user.getId())) {
            throw new TransferException("Source card does not belong to user");
        }

        if (!toCard.getUser().getId().equals(user.getId())) {
            throw new TransferException("Destination card does not belong to user");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new TransferException("Source card is not active");
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new TransferException("Destination card is not active");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("Amount must be positive");
        }

        BigDecimal fromNewBalance = fromCard.getBalance().subtract(amount);
        BigDecimal toNewBalance = toCard.getBalance().add(amount);

        cardService.updateBalance(fromCardId, fromNewBalance);
        cardService.updateBalance(toCardId, toNewBalance);

        System.out.println("TransferService: Перевод выполнен успешно");
        System.out.println("   С карты " + fromCardId + " -> на карту " + toCardId);
        System.out.println("   Сумма: " + amount);
        System.out.println("   Новый баланс исходной карты: " + fromNewBalance);
        System.out.println("   Новый баланс целевой карты: " + toNewBalance);
    }
}