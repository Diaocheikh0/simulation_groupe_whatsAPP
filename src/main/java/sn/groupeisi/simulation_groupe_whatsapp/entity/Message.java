package sn.groupeisi.simulation_groupe_whatsapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String contenu;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User expediteur;
}
