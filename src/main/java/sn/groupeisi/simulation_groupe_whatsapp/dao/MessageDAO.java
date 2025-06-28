package sn.groupeisi.simulation_groupe_whatsapp.dao;

import sn.groupeisi.simulation_groupe_whatsapp.entity.Message;
import sn.groupeisi.simulation_groupe_whatsapp.entity.User;

import java.util.List;

public interface MessageDAO {

    Message createMessage(String contenu, User user);

    List<Message> findAllMessages();

    List<Message> findMessagesByUser(User user);
}
