package com.bank.bank_app.repository;

import com.bank.bank_app.entity.Card;
import com.bank.bank_app.entity.CardStatus;
import com.bank.bank_app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUser(User user);

    Page<Card> findByUser(User user, Pageable pageable);

    Optional<Card> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findByUserAndStatus(User user, CardStatus status);

    Page<Card> findByUserAndOwnerNameContainingIgnoreCase(User user, String ownerName, Pageable pageable);
    Page<Card> findByUserAndStatus(User user, CardStatus status, Pageable pageable);
    Page<Card> findByUserAndOwnerNameContainingIgnoreCaseAndStatus(User user, String ownerName, CardStatus status, Pageable pageable);
}