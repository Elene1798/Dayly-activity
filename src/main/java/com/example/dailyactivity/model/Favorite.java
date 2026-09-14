package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "activity_id"})
        }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    // Снимок занятия на момент добавления в избранное.
    @Column(length = 2000)
    private String snapshotTitle;
    @Column(length = 2000)
    private String snapshotDescription;
    private String snapshotCategory;
    private Integer snapshotDuration;
    private String snapshotLocation;
    @Column(length = 5000)
    private String snapshotInstructions;
    @Column(length = 2000)
    private String snapshotBenefit;
    @Column(length = 2000)
    private String snapshotInterestingFact;
    @Column(length = 2000)
    private String snapshotImageUrl;
    @Column(length = 2000)
    private String snapshotLinkUrl;
    @Column(length = 2000)
    private String snapshotVideoUrl;

    public Favorite() {
    }

    public Favorite(User user, Activity activity) {
        this.user = user;
        this.activity = activity;
        copyFromActivity(activity);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Activity getActivity() { return activity; }

    public String getSnapshotTitle() { return snapshotTitle; }
    public String getSnapshotDescription() { return snapshotDescription; }
    public String getSnapshotCategory() { return snapshotCategory; }
    public Integer getSnapshotDuration() { return snapshotDuration; }
    public String getSnapshotLocation() { return snapshotLocation; }
    public String getSnapshotInstructions() { return snapshotInstructions; }
    public String getSnapshotBenefit() { return snapshotBenefit; }
    public String getSnapshotInterestingFact() { return snapshotInterestingFact; }
    public String getSnapshotImageUrl() { return snapshotImageUrl; }
    public String getSnapshotLinkUrl() { return snapshotLinkUrl; }
    public String getSnapshotVideoUrl() { return snapshotVideoUrl; }

    public boolean hasSnapshot() {
        return snapshotTitle != null;
    }

    public void copyFromActivity(Activity a) {
        this.snapshotTitle = a.getTitle();
        this.snapshotDescription = a.getDescription();
        this.snapshotCategory = a.getCategory();
        this.snapshotDuration = a.getDuration();
        this.snapshotLocation = a.getLocation();
        this.snapshotInstructions = a.getInstructions();
        this.snapshotBenefit = a.getBenefit();
        this.snapshotInterestingFact = a.getInterestingFact();
        this.snapshotImageUrl = a.getImageUrl();
        this.snapshotLinkUrl = a.getLinkUrl();
        this.snapshotVideoUrl = a.getVideoUrl();
    }
}