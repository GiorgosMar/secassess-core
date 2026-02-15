package org.secassess.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Organization entity representing a business entity or client within the security assessment platform.
 */
@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseAuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 64, unique = true)
    private String slug;
}