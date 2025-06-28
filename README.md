# 💬 Simulation d'un Groupe WhatsApp

> **Projet Java Avancé - Semestre 6**  
> **L3IAGE & L3GDA - ISI**  
> **Date de soutenance :** 09/07/2025  
> **Encadrant :** M. GAYE Abdoulaye

## 📋 Description du Projet

Ce projet implémente une simulation complète d'un groupe WhatsApp avec communication en temps réel, interface graphique moderne et système de modération automatique. L'application permet à plusieurs utilisateurs de communiquer dans un chat de groupe avec des fonctionnalités avancées de gestion et de sécurité.

## 🛠️ Technologies Utilisées

- **Communication réseau :** Sockets Java (TCP)
- **Interface graphique :** JavaFX + Scene Builder
- **Base de données :** MySQL / PostgreSQL
- **Accès aux données :** JPA / Hibernate
- **Utilitaires :** Lombok (réduction du code boilerplate)
- **Gestionnaire de dépendances :** Maven / Gradle

## ⭐ Fonctionnalités Principales

### 👥 Gestion des Membres
- **Limitation à 7 membres actifs maximum**
- Gestion des pseudos uniques avec vérification en base
- Possibilité de quitter le groupe volontairement
- Messages d'erreur clairs en cas de groupe plein

### 💬 Système de Messagerie
- **Envoi de messages en temps réel** à tous les membres (sauf expéditeur)
- **Horodatage** automatique des messages avec pseudo de l'expéditeur
- **Notifications** sonores ou pop-up pour les nouveaux messages
- **Historique** des 15 derniers messages chargé à la connexion

### 🔒 Modération Automatique
- **Filtrage des messages injurieux** avec liste noire configurable
- **Bannissement automatique** en cas de contenu inapproprié
- **Notification** de bannissement à tous les membres
- Liste des mots interdits : `GENOCID`, `TERRORISM`, `ATTACK`, `CHELSEA`, `JAVA NEKHOUL`

### 💾 Persistance des Données
- **Sauvegarde automatique** de tous les messages et membres
- **Entités JPA** avec relations définies (OneToMany)
- **Gestion des utilisateurs bannis** avec mise à jour en temps réel

## 🏗️ Architecture du Projet

```
src/main/java/sn/groupeisi/simulation_groupe_whatsapp/
├── entity/              # Entités JPA (User, Message)
├── dao/                 # Data Access Objects
│   ├── UserDAO.java
│   ├── MessageDAO.java
│   └── impl/           # Implémentations DAO
├── service/            # Logique métier
│   ├── ChatService.java
│   └── BanService.java
├── serveur/            # Serveur et client
│   ├── Server.java
│   └── ClientConnect.java
└── ui/                 # Interface JavaFX
    └── controllers/
```

## 🧪 Tests

### Scénarios de Test
1. **Connexion de 7 utilisateurs** - Vérifier la limitation
2. **Envoi de messages** - Test de diffusion
3. **Messages injurieux** - Test de bannissement automatique
4. **Reconnexion** - Test de persistance des données
5. **Historique** - Vérification du chargement des derniers messages

## 📚 Documentation

### Modèle de Données (JPA)

**Entité User**
- id (Long) - Clé primaire
- pseudo (String) - Unique
- banni (Boolean) - Statut de bannissement
- dateInscription (LocalDateTime)

**Entité Message**
- id (Long) - Clé primaire
- contenu (String) - Texte du message
- dateEnvoi (LocalDateTime) - Horodatage
- user (User) - Relation ManyToOne

## 🤝 Contribution

Ce projet est réalisé dans le cadre académique. Pour toute suggestion ou amélioration :

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/amelioration`)
3. Commit les changements (`git commit -m 'Ajout d'une fonctionnalité'`)
4. Push vers la branche (`git push origin feature/amelioration`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est réalisé à des fins éducatives dans le cadre du cursus L3IAGE & L3GDA.

## 👨‍💻 Auteur

**[Votre Nom]**  
- GitHub: [@Diaocheikh0](https://github.com/Diaocheikh0)

## 🙏 Remerciements

- **M. GAYE Abdoulaye** - Encadrant du projet
- **Institut Supérieur d'Informatique (ISI)**


