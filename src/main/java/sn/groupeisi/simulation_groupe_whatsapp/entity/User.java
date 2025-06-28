package sn.groupeisi.simulation_groupe_whatsapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pseudo", unique = true, nullable = false, length = 50)
    private String pseudo;

    @Column(nullable = false)
    private boolean banni = false;
}
