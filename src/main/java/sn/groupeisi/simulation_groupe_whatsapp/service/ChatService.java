package sn.groupeisi.simulation_groupe_whatsapp.service;

import sn.groupeisi.simulation_groupe_whatsapp.dao.MessageDAO;
import sn.groupeisi.simulation_groupe_whatsapp.dao.UserDAO;
import sn.groupeisi.simulation_groupe_whatsapp.entity.Message;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.util.List;

public class ChatService {

    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final BanService banService;

    /**
     * Constructeur du service de chat
     * Initialise les dépendances et crée le service de bannissement
     *
     * @param userDAO DAO pour les opérations sur les utilisateurs
     * @param messageDAO DAO pour les opérations sur les messages
     */
    public ChatService(UserDAO userDAO, MessageDAO messageDAO) {
        this.userDAO = userDAO;
        this.messageDAO = messageDAO;

        this.banService = new BanService(userDAO);
    }

    /**
     * Gère l'adhésion d'un utilisateur au groupe de chat
     * Vérifie les contraintes : pseudo unique et limite de 7 utilisateurs actifs
     *
     * @param pseudo Nom d'utilisateur souhaité
     * @return User créé ou existant si succès, null si échec (groupe plein ou contraintes non respectées)
     */
    public User rejoindreGroupe(String pseudo) {

        User existant = userDAO.findByPseudo(pseudo);

        if (existant != null) {
            return existant;
        }

        // Récupération de tous les membres pour vérifier la limite
        List<User> membres = userDAO.findAll();

        // Comptage des utilisateurs actifs (non bannis)
        long actifs = membres.stream().filter(u -> !u.isBanni()).count();

        // Vérification de la limite de 7 utilisateurs actifs
        if (actifs >= 7) return null;

        // Création d'un nouvel utilisateur si les contraintes sont respectées
        return userDAO.createUser(pseudo);
    }

    /**
     * Traite l'envoi d'un message avec vérification de contenu
     * Applique la modération automatique en cas de contenu inapproprié
     *
     * @param contenu Texte du message à envoyer
     * @param user Utilisateur expéditeur du message
     * @return Message créé si valide, null si utilisateur banni pour contenu inapproprié
     */
    public Message envoyerMessage(String contenu, User user) {
        // Vérification du contenu via le service de modération
        if (banService.contientInjure(contenu)) {
            // Bannissement automatique en cas de contenu inapproprié
            banService.bannir(user);
            return null;
        }

        // Création et persistance du message si le contenu est approprié
        return messageDAO.createMessage(contenu, user);
    }

    /**
     * Récupère l'historique complet des messages du chat
     *
     * @return Liste de tous les messages stockés en base
     */
    public List<Message> historiqueMessage() {
        return messageDAO.findAllMessages();
    }

    /**
     * Supprime définitivement un utilisateur du système
     *
     * @param id Identifiant unique de l'utilisateur à supprimer
     * @return true si la suppression a réussi
     */
    public boolean deleteUser(Long id) {
        userDAO.deleteUser(id);
        return true;
    }
}
