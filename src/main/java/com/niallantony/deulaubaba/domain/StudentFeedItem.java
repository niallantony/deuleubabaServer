package com.niallantony.deulaubaba.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class StudentFeedItem {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER, targetClass = StudentFeedEmotion.class)
    @CollectionTable(name = "student_feed_emotions", joinColumns = @JoinColumn(name = "feed_item_id"))
    @Column(name = "emotion")
    private Set<StudentFeedEmotion> emotions = new HashSet<>();

    private String body;

    private LocalDateTime createdAt;
}
