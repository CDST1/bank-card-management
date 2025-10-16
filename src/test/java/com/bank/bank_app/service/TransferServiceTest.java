package com.bank.bank_app.service;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import com.bank.bank_app.exception.InsufficientFundsException;
import com.bank.bank_app.exception.TransferException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferBetweenOwnCards_Success() {
        User user = new User();
        user.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        transferService.transferBetweenOwnCards(user, 1L, 2L, new BigDecimal("200.00"));

        verify(cardService).updateBalance(1L, new BigDecimal("800.00"));
        verify(cardService).updateBalance(2L, new BigDecimal("700.00"));
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds() {
        User user = new User();
        user.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        assertThrows(InsufficientFundsException.class, () -> {
            transferService.transferBetweenOwnCards(user, 1L, 2L, new BigDecimal("200.00"));
        });
    }

    @Test
    void transferBetweenOwnCards_CardNotActive() {
        User user = new User();
        user.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.BLOCKED);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        assertThrows(TransferException.class, () -> {
            transferService.transferBetweenOwnCards(user, 1L, 2L, new BigDecimal("200.00"));
        });
    }
}