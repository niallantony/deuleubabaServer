package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"users", "student", "createdBy"})
public class Project extends HasImage{
    @Id
    @SequenceGenerator(
            name="project_seq_gen",
            sequenceName = "project_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "project_seq_gen"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String objective;

    @Column(name = "started_on", nullable = false)
    @EqualsAndHashCode.Include
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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectUser> users = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by", referencedColumnName = "userId")
    @JsonBackReference
    private User createdBy;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ProjectFeedItem> feed = new ArrayList<>();

    public void setUsers(Set<ProjectUser> users) {
        this.users = users;
        users.forEach(user -> user.setProject(this));
    }

    @Override
    public String getImage() {
        return imgsrc;
    }

    @Override
    public void setImage(String image) {
        imgsrc = image;
    }
}
