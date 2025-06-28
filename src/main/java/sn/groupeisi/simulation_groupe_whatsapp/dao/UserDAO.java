package sn.groupeisi.simulation_groupe_whatsapp.dao;

import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.util.List;

public interface UserDAO {
    User createUser(String pseudo);

    User findByPseudo(String pseudo);

    List<User> findAll();

    void banUser(User user);

    void deleteUser(Long id);
}
