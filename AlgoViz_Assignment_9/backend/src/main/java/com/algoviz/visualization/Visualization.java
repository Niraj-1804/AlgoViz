package com.algoviz.visualization;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Visualization {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String algorithm;
    @Column(nullable=false,length=2000) private String inputValues;
    @Column(nullable=false) private Instant createdAt;
    protected Visualization() {}
    public Visualization(String algorithm,String inputValues) {
        this.algorithm=algorithm; this.inputValues=inputValues; this.createdAt=Instant.now();
    }
    public Long getId(){return id;}
    public String getAlgorithm(){return algorithm;}
    public String getInputValues(){return inputValues;}
    public Instant getCreatedAt(){return createdAt;}
}
