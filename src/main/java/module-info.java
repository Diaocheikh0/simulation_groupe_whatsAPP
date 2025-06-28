module sn.groupeisi.simulation_groupe_whatsapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;
    requires static lombok;
    requires java.management;
    requires jdk.dynalink;
    requires TrayNotification;

    // Ouvrir les packages pour JavaFX FXML
    opens sn.groupeisi.simulation_groupe_whatsapp to javafx.fxml;
    exports sn.groupeisi.simulation_groupe_whatsapp;

    // Ouvrir les packages d'entités à Hibernate pour la réflexion
    exports sn.groupeisi.simulation_groupe_whatsapp.entity;
    opens sn.groupeisi.simulation_groupe_whatsapp.entity to org.hibernate.orm.core;

    // Ajoutez cette ligne pour exporter le package controller
    exports sn.groupeisi.simulation_groupe_whatsapp.controller to javafx.fxml;
    opens sn.groupeisi.simulation_groupe_whatsapp.controller to javafx.fxml;

}