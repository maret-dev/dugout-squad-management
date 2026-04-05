package com.dugout.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "event_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    @Builder.Default
    private boolean present = false;
}
