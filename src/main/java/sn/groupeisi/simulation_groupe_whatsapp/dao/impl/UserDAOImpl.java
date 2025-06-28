package sn.groupeisi.simulation_groupe_whatsapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import sn.groupeisi.simulation_groupe_whatsapp.dao.UserDAO;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.util.List;

public class UserDAOImpl implements UserDAO {

    private final EntityManagerFactory emf;

    public UserDAOImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Crée un nouvel utilisateur avec un pseudo donné
     * @param pseudo Le pseudo de l'utilisateur
     * @return L'utilisateur créé avec son ID généré
     */
    @Override
    public User createUser(String pseudo) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = new User();
        user.setPseudo(pseudo);

        em.persist(user);

        em.getTransaction().commit();
        em.close();

        return user;
    }

    /**
     * Recherche un utilisateur par son pseudo
     * @param pseudo Le pseudo à rechercher
     * @return L'utilisateur trouvé ou null si aucun utilisateur avec ce pseudo
     */
    @Override
    public User findByPseudo(String pseudo) {
        EntityManager em = emf.createEntityManager();

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.pseudo = :pseudo", User.class);
        query.setParameter("pseudo", pseudo);
        List<User> users = query.getResultList();

        em.close();

        // Retourner le premier utilisateur trouvé, ou null si la liste est vide
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Récupère tous les utilisateurs de la base de données
     * @return Liste complète des utilisateurs
     */
    @Override
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<User> query = em.createQuery("SELECT t FROM User t", User.class);
        List<User> users = query.getResultList();

        em.close();

        return users;
    }

    /**
     * Bannit un utilisateur (marque comme banni sans le supprimer)
     * @param user L'utilisateur à bannir
     */
    @Override
    public void banUser(User user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        // Récupérer l'entité managée pour pouvoir la modifier
        User managedUser = em.merge(user);
        managedUser.setBanni(true);

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Recherche un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé ou null si inexistant
     */
    public User findById(Long id) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, id);
        em.close();
        return user;
    }

    /**
     * Supprime définitivement un utilisateur et tous ses messages
     * @param id L'ID de l'utilisateur à supprimer
     */
    @Override
    public void deleteUser(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, id);
        if (user != null) {
            // D'abord supprimer tous les messages de cet utilisateur
            em.createQuery("DELETE FROM Message m WHERE m.expediteur.id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();

            // Ensuite supprimer l'utilisateur
            em.remove(user);
        }

        em.getTransaction().commit();
        em.close();
    }
}
