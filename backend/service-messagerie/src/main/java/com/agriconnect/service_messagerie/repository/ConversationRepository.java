package com.agriconnect.service_messagerie.repository;

import com.agriconnect.service_messagerie.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    /**
     * Trouve la conversation entre 2 utilisateurs.
     * Cherche dans les 2 sens (A→B ou B→A).
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.idSender = :user1 AND c.idReceiver = :user2) OR " +
           "(c.idSender = :user2 AND c.idReceiver = :user1)")
    Optional<Conversation> findByUsers(@Param("user1") Integer user1, @Param("user2") Integer user2);

    /**
     * Toutes les conversations d'un utilisateur.
     */
    @Query("SELECT c FROM Conversation c WHERE c.idSender = :userId OR c.idReceiver = :userId " +
           "ORDER BY c.dateCreation DESC")
    List<Conversation> findAllByUser(@Param("userId") Integer userId);
}