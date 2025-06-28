package sn.groupeisi.simulation_groupe_whatsapp.controller;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.MessageDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.dao.impl.UserDAOImpl;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;
import sn.groupeisi.simulation_groupe_whatsapp.service.ChatService;
import sn.groupeisi.simulation_groupe_whatsapp.tools.Notification;

import java.io.IOException;

public class loginController {

    private final ChatService chatService;

    @FXML
    private TextField pseudoTfd;

    public loginController() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("simulationwhatsapp");
        this.chatService = new ChatService(
                new UserDAOImpl(emf),
                new MessageDAOImpl(emf)
        );
    }

    @FXML
    void loginController(ActionEvent event) throws IOException {
        String pseudo = pseudoTfd.getText().trim();

        if (pseudo.isEmpty()) {
            Notification.NotifError("Erreur", "Veuillez renseigner un pseudo");
            return;
        }

        User user = chatService.rejoindreGroupe(pseudo);

        if (user == null) {
            Notification.NotifError("Erreur", "Ce pseudo est déjà utilisé ou le groupe est plein !");
        } else if (user.isBanni()) {
            Notification.NotifError("Accès refusé", "Vous êtes banni du groupe.");
        } else {
            // Charger l'écran de chat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
            Parent root = loader.load();

            chatController controller = loader.getController();
            controller.setUserConnecte(user); // Passer l'utilisateur

            // Charger l'historique depuis la DB
            controller.chargerHistorique();

            // Initialiser la connexion socket
            controller.initializeSocketConnection();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Gérer la fermeture de la fenêtre pour déconnecter le socket
            stage.setOnCloseRequest(e -> {
                controller.onWindowClose();
            });

            stage.setScene(new Scene(root));
            stage.setTitle("Chat - " + user.getPseudo());
            stage.show();

            System.out.println("Interface de chat ouverte pour: " + user.getPseudo());
        }
    }
}