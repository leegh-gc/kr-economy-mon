package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "visitor_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VisitorCount {

    @Id
    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(nullable = false)
    private long count;

    public void increment() {
        this.count++;
    }
}
