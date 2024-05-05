package dev.system.backend.commons;

import jakarta.persistence.*;

@Entity
public class AnomalyScore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private float score;
    @ManyToOne
    private Ship ship;
    private String info;

    public AnomalyScore(float score, String info) {
        this.score = score;
        this.info = info;
    }

    public float getScore() {
        return score;
    }

    public String getInfo() {
        return info;
    }
}
