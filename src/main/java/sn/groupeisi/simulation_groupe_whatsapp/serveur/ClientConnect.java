package sn.groupeisi.simulation_groupe_whatsapp.serveur;

import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Classe client pour la connexion au serveur de chat WhatsApp
 * Gère la connexion, l'envoi/réception de messages et les callbacks
 */
public class ClientConnect {
    // Socket de connexion au serveur
    private Socket socket;

    // Flux de sortie pour envoyer des messages au serveur
    private PrintWriter out;

    // Flux d'entrée pour recevoir des messages du serveur
    private BufferedReader in;

    // Thread dédié à l'écoute des messages du serveur
    private Thread lecteurThread;

    // Callback fonctionnel pour traiter les messages reçus du serveur
    // Utilise Lombok @Setter pour générer automatiquement le setter
    @Setter
    private Consumer<String> onMessageReceived;

    // Configuration de connexion au serveur
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    /**
     * Établit la connexion au serveur avec authentification
     *
     * @param username Nom d'utilisateur pour l'authentification
     * @return true si la connexion et l'authentification réussissent, false sinon
     */
    public boolean connect(String username) {
        try {
            // Création de la socket de connexion
            socket = new Socket(HOST, PORT);

            // Initialisation des flux de communication
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connecté au serveur " + HOST + ":" + PORT);

            // Envoi du nom d'utilisateur au serveur selon le protocole défini
            out.println("USERNAME:" + username);

            // Lecture de la réponse du serveur pour confirmer l'authentification
            String confirmation = in.readLine();
            System.out.println("Serveur: " + confirmation);

            // Vérification du succès de l'authentification
            if (!confirmation.startsWith("BIENVENUE")) {
                System.out.println("Erreur d'authentification");
                disconnect();
                return false;
            }

            // Démarrage du thread d'écoute des messages du serveur
            startMessageListener();

            return true;

        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le client est actuellement connecté au serveur
     *
     * @return true si la socket existe et n'est pas fermée, false sinon
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    /**
     * Démarre un thread dédié à l'écoute des messages provenant du serveur
     * Le thread fonctionne en arrière-plan et traite les messages via le callback
     */
    private void startMessageListener() {
        lecteurThread = new Thread(() -> {
            try {
                String messageServeur;

                // Boucle d'écoute continue des messages du serveur
                while ((messageServeur = in.readLine()) != null) {
                    System.out.println("Reçu du serveur: " + messageServeur);

                    // Exécution du callback si défini pour traiter le message
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(messageServeur);
                    }
                }
            } catch (IOException e) {
                // Éviter d'afficher l'erreur si la socket a été fermée intentionnellement
                if (!socket.isClosed()) {
                    System.err.println("Erreur lecture serveur : " + e.getMessage());
                }
            }
        });

        // Configuration du thread comme daemon pour qu'il se ferme avec l'application
        lecteurThread.setDaemon(true);
        lecteurThread.start();
    }

    /**
     * Envoie un message au serveur
     * Vérifie l'état de la connexion avant l'envoi
     *
     * @param message Contenu du message à envoyer
     */
    public void sendMessage(String message) {
        // Vérification de l'état de la connexion
        if (out != null && !socket.isClosed()) {
            out.println(message);
            System.out.println("Message envoyé: " + message);
        } else {
            System.err.println("Connexion fermée, impossible d'envoyer: " + message);
        }
    }

    /**
     * Ferme proprement la connexion au serveur
     * Nettoie toutes les ressources (thread, flux, socket)
     */
    public void disconnect() {
        try {
            // Interruption du thread de lecture s'il existe
            if (lecteurThread != null) {
                lecteurThread.interrupt();
            }

            // Fermeture du flux de sortie
            if (out != null) {
                out.close();
            }

            // Fermeture du flux d'entrée
            if (in != null) {
                in.close();
            }

            // Fermeture de la socket si elle est encore ouverte
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Connexion fermée");
        } catch (IOException e) {
            System.err.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}