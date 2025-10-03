package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String objective;

    @Column(nullable = false)
    private LocalDate startedOn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_communication_category",
            joinColumns = @JoinColumn(name= "project_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
    )
    @JsonManagedReference
    private Set<CommunicationCategory> categories = new HashSet<>();

    private String description;

    private String imgsrc;

    @Column(nullable = false)
    private Boolean completed = false;

    private LocalDate completedOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectUser> users = new HashSet<>();
}
