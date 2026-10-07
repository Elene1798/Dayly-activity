package com.example.dailyactivity.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_visits",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_visit_date",
                        columnNames = {"user_id", "visit_date"}
                )
        }
)
public class UserVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    public UserVisit() {
    }

    public UserVisit(User user, LocalDate visitDate) {
        this.user = user;
        this.visitDate = visitDate;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }
}