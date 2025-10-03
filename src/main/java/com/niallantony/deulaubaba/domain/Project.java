package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String objective;

    @Column(nullable = false)
    private Date startDate;

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
    private Boolean completed;

    private Date completedOn;

    @Column(nullable = false)
    private ProjectType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectUser> users = new HashSet<>();
}
