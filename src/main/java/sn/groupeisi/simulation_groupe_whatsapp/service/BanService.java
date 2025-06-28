package sn.groupeisi.simulation_groupe_whatsapp.service;

import sn.groupeisi.simulation_groupe_whatsapp.dao.UserDAO;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.util.Arrays;
import java.util.List;

/**
 * Service responsable de la modération du chat
 * Gère la détection de contenus inappropriés et les bannissements d'utilisateurs
 */
public class BanService {

    // DAO pour la gestion des utilisateurs en base de données
    private final UserDAO userDAO;

    // Liste des mots interdits qui déclenchent un bannissement automatique
    // Cette liste peut être étendue selon les besoins de modération
    private final List<String> motsInterdits = Arrays.asList(
            "génocide", "terrorisme", "attentat", "chelsea", "java nékhoul"
    );

    /**
     * Constructeur du service de bannissement
     *
     * @param userDAO DAO pour accéder aux données des utilisateurs
     */
    public BanService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Vérifie si un contenu contient des mots interdits
     * La vérification est insensible à la casse
     *
     * @param contenu Texte à analyser
     * @return true si le contenu contient au moins un mot interdit, false sinon
     */
    public boolean contientInjure(String contenu) {
        // Conversion en minuscules pour une comparaison insensible à la casse
        String texte = contenu.toLowerCase();

        // Utilisation des streams pour vérifier la présence de mots interdits
        return motsInterdits.stream().anyMatch(texte::contains);
    }

    /**
     * Bannit un utilisateur du chat
     * Met à jour le statut de bannissement et persiste en base
     *
     * @param user Utilisateur à bannir
     */
    public void bannir(User user) {
        // Mise à jour du statut de bannissement
        user.setBanni(true);

        // Persistance du bannissement en base de données
        userDAO.banUser(user);
    }
}