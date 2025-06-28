package sn.groupeisi.simulation_groupe_whatsapp.controller;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.MessageDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.UserDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.entity.Message;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;
import sn.groupeisi.simulation_groupe_whatsapp.serveur.ClientConnect;
import sn.groupeisi.simulation_groupe_whatsapp.service.ChatService;
import sn.groupeisi.simulation_groupe_whatsapp.tools.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class chatController {

    private ClientConnect clientConnect;
    @Setter
    private User userConnecte;
    private final ChatService chatService;
    @FXML
    private TextField messageTfd;
    @FXML
    private VBox chatContainer;

    public chatController() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("simulationwhatsapp");
        this.chatService = new ChatService(
                new UserDAOImpl(emf),
                new MessageDAOImpl(emf)
        );
    }

    public void initialize() {
        messageTfd.setDisable(true);
    }

    // Cette méthode sera appelée depuis loginController après connexion réussie
    public void initializeSocketConnection() {
        if (userConnecte == null) {
            System.err.println("Erreur: userConnecte est null");
            return;
        }

        clientConnect = new ClientConnect();

        // Pour recevoir les messages du serveur
        clientConnect.setOnMessageReceived(this::afficherMessage);

        // Connexion au serveur dans un thread séparé
        new Thread(() -> {
            boolean connected = clientConnect.connect(userConnecte.getPseudo());

            Platform.runLater(() -> {
                if (connected) {
                    messageTfd.setDisable(false);
                    System.out.println("Connexion réussie pour " + userConnecte.getPseudo());
                    Notification.NotifSuccess("Success", "Connexion réussie pour " + userConnecte.getPseudo());
                } else {
                    Notification.NotifError("Erreur", "Impossible de se connecter au serveur");
                }
            });
        }).start();
    }

    // Charge l'historique des messages une fois l'utilisateur connecté
    public void chargerHistorique() {
        if (userConnecte == null) {
            System.err.println("Erreur: userConnecte est null dans chargerHistorique");
            return;
        }

        List<Message> historique = chatService.historiqueMessage();

        // Vider le container
        chatContainer.getChildren().clear();

        // Afficher tous les messages retournés
        for (Message msg : historique) {
            afficherMessageFormate(msg.getExpediteur().getPseudo(), msg.getContenu(), msg.getDateEnvoi());
        }
    }

    //Méthode pour l'envoi de message
    @FXML
    void sendMessage(ActionEvent event) {
        String contenu = messageTfd.getText().trim();

        if (contenu.isEmpty()) {
            Notification.NotifError("Erreur", "Veuillez écrire un message !");
            return;
        }

        // Vérifier si la connexion socket est active
        if (clientConnect == null || !clientConnect.isConnected()) {
            Notification.NotifError("Erreur", "Connexion au serveur perdue. Veuillez redémarrer l'application !");
            return;
        }

        // Envoyer via socket au serveur
        clientConnect.sendMessage(contenu);
        messageTfd.clear();

        // Affichage du message de l'expéditeur
        Platform.runLater(() -> {
            afficherMessageFormate(userConnecte.getPseudo(), contenu, LocalDateTime.now());
        });
    }

    // Méthode pour quitter le groupe
    @FXML
    void exitGroupe(ActionEvent event) {
        if (userConnecte == null) {
            return;
        }

        chatService.deleteUser(userConnecte.getId());

        onWindowClose();
    }

    // Une seule méthode pour formater tous les messages
    // Que ce soit pour l'historique ou les nouveaux messages
    public void afficherMessageFormate(String expediteur, String message, LocalDateTime dateEnvoi) {
        String heure = dateEnvoi.format(DateTimeFormatter.ofPattern("HH:mm"));
        String date = dateEnvoi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String currentUser = userConnecte.getPseudo();

        HBox messageBox = new HBox();

        // VBox pour contenir le message et l'heure dans la même bulle
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(3);
        messageContainer.setMaxWidth(500);

        Label messageLabel = new Label(expediteur + ": " + message);
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(date + " à " + heure);
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #888888;");

        if (expediteur.equals(currentUser)) {
            // Message envoyé à droite
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.setStyle("-fx-background-color: #dcf8c6; -fx-padding: 8px; -fx-background-radius: 10px;");
        } else {
            // Message reçu à gauche
            messageBox.setAlignment(Pos.CENTER_LEFT);
            timeLabel.setAlignment(Pos.CENTER_LEFT);
            messageContainer.setStyle("-fx-background-color: #ffffff; -fx-padding: 8px; -fx-background-radius: 10px;");
        }

        messageContainer.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(messageContainer);
        chatContainer.getChildren().add(messageBox);
    }

    // Callback appelé quand un nouveau message arrive du serveur
    public void afficherMessage(String ligne) {
        System.out.println("Message reçu du serveur: " + ligne);

        if (ligne.startsWith("BROADCAST: ")) {
            String contenu = ligne.substring(11);

            // Parser le format "[expediteur] message"
            if (contenu.startsWith("[") && contenu.contains("]")) {
                int endBracket = contenu.indexOf("]");
                String expediteur = contenu.substring(1, endBracket);
                String message = contenu.substring(endBracket + 1).trim();
                LocalDateTime dateEnvoi = LocalDateTime.now();
                String heure = dateEnvoi.format(DateTimeFormatter.ofPattern("HH:mm"));
                String date = dateEnvoi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // Mettre à jour l'interface
                Platform.runLater(() -> {
                    // Notification popup
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Nouveau message");
                    alert.setHeaderText(expediteur);
                    alert.setContentText(message + " - " + date + " à " + heure);
                    alert.show();

                    // Ajout dans le chat
                    afficherMessageFormate(expediteur, message, LocalDateTime.now());

                    // Fermer le popUp après 5 seconde
                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.seconds(5),
                            ae -> alert.close()));
                    timeline.play();
                });
            }
        }
    }

    // Méthode appelée à la fermeture de la fenêtre
    public void onWindowClose() {
        if (clientConnect != null) {
            clientConnect.disconnect();
        }
    }
}