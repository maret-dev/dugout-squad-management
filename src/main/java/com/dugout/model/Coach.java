package com.dugout.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coaches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL)
    @Builder.Default
    private List<com.dugout.model.Team> teams = new ArrayList<>();
}
