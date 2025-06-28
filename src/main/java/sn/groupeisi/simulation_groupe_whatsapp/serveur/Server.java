package sn.groupeisi.simulation_groupe_whatsapp.serveur;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.MessageDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.UserDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.entity.Message;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;
import sn.groupeisi.simulation_groupe_whatsapp.service.ChatService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    // Map thread-safe pour stocker les clients connectés (username -> socket)
    private static final Map<String, Socket> clientsConnectes = new ConcurrentHashMap<>();

    // Factory JPA pour la gestion de la persistance des données
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("simulationwhatsapp");

    // Service métier pour gérer les utilisateurs et messages
    private static final ChatService chatService = new ChatService(
            new UserDAOImpl(emf),
            new MessageDAOImpl(emf)
    );

    /**
     * Point d'entrée du serveur
     * Démarre le serveur sur le port 12345 et accepte les connexions clients
     */
    public static void main(String[] args) {
        final int PORT = 12345;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);

            // Boucle infinie pour accepter les nouvelles connexions
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion depuis : " + clientSocket.getInetAddress());

                // Création d'un thread dédié pour chaque client
                new Thread(() -> traiterClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Traite les interactions avec un client spécifique
     * Gère l'authentification, la réception et l'envoi de messages
     *
     * @param clientSocket Socket de connexion du client
     */
    private static void traiterClient(Socket clientSocket) {
        String username = null;
        User user = null;

        try (
                // Flux d'entrée pour lire les messages du client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Flux de sortie pour envoyer des messages au client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Lecture du premier message qui doit contenir le nom d'utilisateur
            String premierMessage = in.readLine();

            // Vérification et traitement de l'authentification
            if (premierMessage != null && premierMessage.startsWith("USERNAME:")) {
                username = premierMessage.substring(9); // Extraction du nom d'utilisateur

                // Tentative de connexion au groupe via le service métier
                user = chatService.rejoindreGroupe(username);
                if (user == null) {
                    // Échec de connexion (groupe plein ou pseudo déjà pris)
                    out.println("ERREUR: Groupe plein ou pseudo déjà pris !");
                    return;
                }

                // Enregistrement du client connecté
                clientsConnectes.put(username, clientSocket);

                // Confirmation de connexion au client
                out.println("BIENVENUE " + username + " ! Vous êtes connecté.");
                System.out.println("Utilisateur connecté : " + username);

                // Notification aux autres clients de l'arrivée du nouvel utilisateur
                diffuserMessage("SYSTÈME", username + " a rejoint le chat", username);

            } else {
                // Premier message invalide
                out.println("ERREUR: Authentification requise !");
                return;
            }

            String messageRecu;

            // Boucle de traitement des messages du client
            while ((messageRecu = in.readLine()) != null) {
                System.out.println("[" + username + "] : " + messageRecu);

                // Commande de déconnexion
                if ("quit".equalsIgnoreCase(messageRecu)) {
                    out.println("Au revoir " + username + " !");
                    break;
                }

                // Traitement du message via le service métier
                Message message = chatService.envoyerMessage(messageRecu, user);

                // Vérification si l'utilisateur a été banni pour message injurieux
                if (message == null && user.isBanni()) {
                    out.println("BANNI: Vous avez été banni pour message injurieux.");
                    break;
                }

                // Diffusion du message à tous les autres clients
                diffuserMessage(username, messageRecu, username);
                out.println("Message envoyé à tous les clients");
            }

        } catch (IOException e) {
            System.err.println("Erreur avec le client " + username + " : " + e.getMessage());
        } finally {
            // Nettoyage lors de la déconnexion du client
            if (username != null) {
                clientsConnectes.remove(username);
                System.out.println("Utilisateur déconnecté : " + username);

                // Notification aux autres clients du départ de l'utilisateur
                diffuserMessage("SYSTÈME", username + " a quitté le chat", username);
            }

            // Fermeture propre du socket
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erreur fermeture socket : " + e.getMessage());
            }
        }
    }

    /**
     * Diffuse un message à tous les clients connectés sauf à l'expéditeur
     *
     * @param expediteur Nom de l'expéditeur du message
     * @param message Contenu du message à diffuser
     * @param expediteurOriginal Nom de l'expéditeur original (pour éviter le renvoi)
     */
    private static void diffuserMessage(String expediteur, String message, String expediteurOriginal) {
        // Formatage du message avec le nom de l'expéditeur
        String messageComplet = "[" + expediteur + "] " + message;

        // Parcours de tous les clients connectés
        for (Map.Entry<String, Socket> entry : clientsConnectes.entrySet()) {
            String nomClient = entry.getKey();
            Socket socketClient = entry.getValue();

            // Éviter de renvoyer le message à son expéditeur
            if (!nomClient.equals(expediteurOriginal)) {
                try {
                    // Envoi du message au client
                    PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
                    out.println("BROADCAST: " + messageComplet);
                } catch (IOException e) {
                    // En cas d'erreur, suppression du client de la liste (déconnecté)
                    System.err.println("Erreur envoi message à " + nomClient + " : " + e.getMessage());
                    clientsConnectes.remove(nomClient);
                }
            }
        }
    }
}