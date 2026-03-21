package com.agriconnect.service_messagerie.service;

import com.agriconnect.service_messagerie.client.UserClient;
import com.agriconnect.service_messagerie.model.Conversation;
import com.agriconnect.service_messagerie.model.Message;
import com.agriconnect.service_messagerie.repository.ConversationRepository;
import com.agriconnect.service_messagerie.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessagerieService {

    private static final Logger log = LoggerFactory.getLogger(MessagerieService.class);

    private final ConversationRepository conversationRepo;
    private final MessageRepository      messageRepo;
    private final UserClient             userClient;

    // ── Conversations ─────────────────────────────────────

    @Transactional
    public Conversation ouvrirConversation(Integer idSender, Integer idReceiver) {
        return conversationRepo.findByUsers(idSender, idReceiver)
                .orElseGet(() -> conversationRepo.save(
                    Conversation.builder()
                        .idSender(idSender)
                        .idReceiver(idReceiver)
                        .type("PRIVE")
                        .build()
                ));
    }

    public List<Map<String, Object>> getMesConversationsEnrichies(Integer idUser, String token) {
        List<Conversation> convs = conversationRepo.findAllByUser(idUser);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Conversation conv : convs) {
            // Ignorer les conversations supprimées pour cet user
            boolean estSender = conv.getIdSender().equals(idUser);
            if (estSender   && Boolean.TRUE.equals(conv.getSupprimeSender()))   continue;
            if (!estSender  && Boolean.TRUE.equals(conv.getSupprimeReceiver())) continue;

            Message dernierMsg = messageRepo.findLastMessage(conv.getId());
            long nonLus        = messageRepo.countNonLus(conv.getId(), idUser);

            Integer idInterlocuteur = estSender ? conv.getIdReceiver() : conv.getIdSender();
            String nomInterlocuteur   = "Utilisateur #" + idInterlocuteur;
            String photoInterlocuteur = null;

            try {
                Map<String, Object> userInfo = userClient.getPublic(idInterlocuteur);
                if (userInfo != null) {
                    nomInterlocuteur   = (String) userInfo.get("nom");
                    photoInterlocuteur = (String) userInfo.get("photo");
                }
            } catch (Exception e) {
                log.warn("User {} introuvable : {}", idInterlocuteur, e.getMessage());
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idConversation",      conv.getId());
            item.put("idSender",            conv.getIdSender());
            item.put("idReceiver",          conv.getIdReceiver());
            item.put("dateCreation",        conv.getDateCreation());
            item.put("nonLus",              nonLus);
            item.put("nomInterlocuteur",    nomInterlocuteur);
            item.put("photoInterlocuteur",  photoInterlocuteur);
            item.put("enLigne",             false);
            // ✅ Épinglé / archivé selon la perspective de l'user courant
            item.put("epingle",  estSender ? conv.getEpingleSender()  : conv.getEpingleReceiver());
            item.put("archive",  estSender ? conv.getArchiveSender()  : conv.getArchiveReceiver());

            if (dernierMsg != null) {
                item.put("dernierMessage",       Boolean.TRUE.equals(dernierMsg.getSupprimeTous()) ? null : dernierMsg.getContenu());
                item.put("dernierMessageType",   dernierMsg.getTypeMessage());
                item.put("dernierMessageDate",   dernierMsg.getDateCreate());
                item.put("dernierMessageStatut", dernierMsg.getStatut());
            }
            result.add(item);
        }

        // Trier : épinglés en premier, puis par date du dernier message
        result.sort((a, b) -> {
            boolean epA = Boolean.TRUE.equals(a.get("epingle"));
            boolean epB = Boolean.TRUE.equals(b.get("epingle"));
            if (epA && !epB) return -1;
            if (!epA && epB) return 1;
            Object da = a.get("dernierMessageDate");
            Object db = b.get("dernierMessageDate");
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.toString().compareTo(da.toString());
        });

        return result;
    }

    // ✅ Épingler / désépingler
    @Transactional
    public void epinglerConversation(Integer idConversation, Integer idUser, boolean epingle) {
        Conversation conv = conversationRepo.findById(idConversation)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        if (conv.getIdSender().equals(idUser))   conv.setEpingleSender(epingle);
        else                                      conv.setEpingleReceiver(epingle);
        conversationRepo.save(conv);
    }

    // ✅ Archiver / désarchiver
    @Transactional
    public void archiverConversation(Integer idConversation, Integer idUser, boolean archive) {
        Conversation conv = conversationRepo.findById(idConversation)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        if (conv.getIdSender().equals(idUser))   conv.setArchiveSender(archive);
        else                                      conv.setArchiveReceiver(archive);
        conversationRepo.save(conv);
    }

    // ✅ Supprimer conversation (pour moi uniquement)
    @Transactional
    public void supprimerConversation(Integer idConversation, Integer idUser) {
        Conversation conv = conversationRepo.findById(idConversation)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        if (conv.getIdSender().equals(idUser))   conv.setSupprimeSender(true);
        else                                      conv.setSupprimeReceiver(true);
        conversationRepo.save(conv);
    }

    // ── Messages ──────────────────────────────────────────

    @Transactional
    public List<Map<String, Object>> getMessages(Integer idConversation, Integer idUser) {
        List<Message> messages = messageRepo.findByConversation(idConversation);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            boolean estSender = msg.getIdSender().equals(idUser);
            if (estSender  && Boolean.TRUE.equals(msg.getSupprimeSender()))    continue;
            if (!estSender && Boolean.TRUE.equals(msg.getSupprimerReceiver())) continue;
            result.add(buildMessageResponse(msg, idUser));
            if (!estSender && "ENVOYE".equals(msg.getStatut())) {
                msg.setStatut("DISTRIBUE");
                messageRepo.save(msg);
            }
        }
        return result;
    }

    @Transactional
    public Message envoyerMessage(Integer idConversation, Integer idSender,
            String contenu, String pieceJointe, String typeMessage, Integer idReply) {
        return messageRepo.save(Message.builder()
                .idConversation(idConversation)
                .idSender(idSender)
                .contenu(contenu)
                .pieceJointe(pieceJointe)
                .typeMessage(typeMessage != null ? typeMessage : "TEXTE")
                .statut("ENVOYE")
                .idReply(idReply)
                .supprimeSender(false)
                .supprimerReceiver(false)
                .supprimeTous(false)
                .build());
    }

    @Transactional
    public void marquerLus(Integer idConversation, Integer idUser) {
        messageRepo.marquerLus(idConversation, idUser);
    }

    @Transactional
    public Map<String, Object> supprimerMessage(Integer idMessage, Integer idUser, boolean pourTous) {
        Message msg = messageRepo.findById(idMessage)
                .orElseThrow(() -> new RuntimeException("Message introuvable"));
        if (pourTous) {
            if (!msg.getIdSender().equals(idUser))
                throw new RuntimeException("Vous ne pouvez supprimer que vos propres messages");
            msg.setSupprimeTous(true);
            msg.setContenu(null);
            msg.setPieceJointe(null);
        } else {
            if (msg.getIdSender().equals(idUser)) msg.setSupprimeSender(true);
            else msg.setSupprimerReceiver(true);
        }
        messageRepo.save(msg);
        return Map.of("idMessage", idMessage, "supprimeTous", pourTous, "statut", "SUPPRIME");
    }

    @Transactional
    public Map<String, Object> reagir(Integer idMessage, Integer idUser, String emoji) {
        Message msg = messageRepo.findById(idMessage)
                .orElseThrow(() -> new RuntimeException("Message introuvable"));
        Map<String, List<Integer>> reactions = parseReactions(msg.getReactions());
        List<Integer> users = reactions.computeIfAbsent(emoji, k -> new ArrayList<>());
        if (users.contains(idUser)) { users.remove(idUser); if (users.isEmpty()) reactions.remove(emoji); }
        else users.add(idUser);
        msg.setReactions(serializeReactions(reactions));
        messageRepo.save(msg);
        return Map.of("idMessage", idMessage, "reactions", reactions);
    }

    // ── Helpers ───────────────────────────────────────────

    private Map<String, Object> buildMessageResponse(Message msg, Integer idUser) {
        Map<String, Object> res = new HashMap<>();
        res.put("idMessage",      msg.getId());
        res.put("idConversation", msg.getIdConversation());
        res.put("idSender",       msg.getIdSender());
        res.put("contenu",        Boolean.TRUE.equals(msg.getSupprimeTous()) ? null : msg.getContenu());
        res.put("pieceJointe",    Boolean.TRUE.equals(msg.getSupprimeTous()) ? null : msg.getPieceJointe());
        res.put("typeMessage",    msg.getTypeMessage());
        res.put("statut",         msg.getStatut());
        res.put("dateCreate",     msg.getDateCreate());
        res.put("idReply",        msg.getIdReply());
        res.put("reactions",      parseReactions(msg.getReactions()));
        res.put("supprimeTous",   msg.getSupprimeTous());
        res.put("estMoi",         msg.getIdSender().equals(idUser));
        return res;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Integer>> parseReactions(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try { return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class); }
        catch (Exception e) { return new HashMap<>(); }
    }

    private String serializeReactions(Map<String, List<Integer>> reactions) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reactions); }
        catch (Exception e) { return "{}"; }
    }
}