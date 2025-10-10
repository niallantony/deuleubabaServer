package com.niallantony.deulaubaba.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ProjectFeedItem {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String body;

    private LocalDateTime createdAt;
}
