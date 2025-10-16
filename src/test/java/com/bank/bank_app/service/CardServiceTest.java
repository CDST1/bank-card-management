package com.bank.bank_app.service;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.exception.CardNotFoundException;
import com.bank.bank_app.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    @Test
    void getCardById_CardExists() {
        Card card = new Card();
        card.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Card result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCardById_CardNotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            cardService.getCardById(999L);
        });
    }

    @Test
    void blockCard_Success() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_Success() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository).save(card);
    }
}