package sn.groupeisi.simulation_groupe_whatsapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import sn.groupeisi.simulation_groupe_whatsapp.dao.MessageDAO;
import sn.groupeisi.simulation_groupe_whatsapp.entity.Message;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class MessageDAOImpl implements MessageDAO {
    private final EntityManagerFactory emf;

    public MessageDAOImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // Crée et sauvegarde un nouveau message en base de données
    @Override
    public Message createMessage(String contenu, User user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Message message = new Message();
        message.setContenu(contenu);
        message.setDateEnvoi(LocalDateTime.now());
        message.setExpediteur(user);

        em.persist(message);
        em.getTransaction().commit();
        em.close();

        return message;
    }

    /**
     * Récupère les 15 messages les plus récents pour l'historique du chat
     * Les messages sont triés par ordre chronologique (du plus ancien au plus récent)
     * @return Liste des 15 derniers messages
     */
    @Override
    public List<Message> findAllMessages() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Message> query = em.createQuery(
                "SELECT m FROM Message m ORDER BY m.dateEnvoi DESC",
                Message.class
        );
        query.setMaxResults(15);
        List<Message> messages = query.getResultList();

        // Inverser pour afficher du plus ancien au plus récent
        Collections.reverse(messages);

        em.close();

        return messages;
    }

    /**
     * Récupère tous les messages envoyés par un utilisateur spécifique
     * @param user L'utilisateur dont on veut récupérer les messages
     * @return Liste des messages de cet utilisateur
     */
    @Override
    public List<Message> findMessagesByUser(User user) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Message> query = em.createQuery(
                "SELECT m FROM Message m WHERE m.expediteur = :user", Message.class);
        query.setParameter("user", user);
        List<Message> messages = query.getResultList();
        em.close();
        return messages;
    }
}
