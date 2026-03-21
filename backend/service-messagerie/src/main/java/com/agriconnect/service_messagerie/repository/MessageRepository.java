package com.agriconnect.service_messagerie.repository;

import com.agriconnect.service_messagerie.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Messages d'une conversation — ordonnés par date.
     * Exclut les messages supprimés pour tous.
     */
    @Query("SELECT m FROM Message m WHERE m.idConversation = :idCo AND m.supprimeTous = false " +
           "ORDER BY m.dateCreate ASC")
    List<Message> findByConversation(@Param("idCo") Integer idCo);

    /**
     * Marquer tous les messages non lus d'une conversation comme LU.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.statut = 'LU' WHERE m.idConversation = :idCo " +
           "AND m.idSender != :idUser AND m.statut != 'LU'")
    void marquerLus(@Param("idCo") Integer idCo, @Param("idUser") Integer idUser);

    /**
     * Nombre de messages non lus pour un utilisateur dans une conversation.
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.idConversation = :idCo " +
           "AND m.idSender != :idUser AND m.statut != 'LU' AND m.supprimeTous = false")
    long countNonLus(@Param("idCo") Integer idCo, @Param("idUser") Integer idUser);

    /**
     * Dernier message d'une conversation.
     */
    @Query("SELECT m FROM Message m WHERE m.idConversation = :idCo AND m.supprimeTous = false " +
           "ORDER BY m.dateCreate DESC LIMIT 1")
    Message findLastMessage(@Param("idCo") Integer idCo);
}