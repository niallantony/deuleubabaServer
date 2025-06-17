package com.niallantony.deulaubaba;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DictionaryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpressionType expressionType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CommunicationCategory category;

    private String imageSrc;

    private String description;


}
