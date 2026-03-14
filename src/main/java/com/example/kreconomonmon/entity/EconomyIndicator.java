package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "economy_indicator",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_economy_indicator",
        columnNames = {"stat_code", "item_code", "period"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EconomyIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_code", nullable = false, length = 20)
    private String statCode;

    @Column(name = "item_code", nullable = false, length = 30)
    private String itemCode;

    @Column(name = "period", nullable = false, length = 10)
    private String period;

    @Column(name = "value", precision = 20, scale = 4)
    private BigDecimal value;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** upsert 시 값 갱신용 메서드 */
    public void updateValue(BigDecimal newValue) {
        this.value = newValue;
    }
}
