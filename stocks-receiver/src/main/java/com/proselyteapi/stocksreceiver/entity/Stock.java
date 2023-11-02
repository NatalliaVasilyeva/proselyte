package com.proselyteapi.stocksreceiver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table("stock")
public class Stock implements Persistable<Long> {

    @Id
    private Long id;

    private String symbol;

    private String companyName;

    private BigDecimal latestPrice;

    private BigDecimal change;

    private BigDecimal changePercent;

    private Instant latestUpdate;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return null == getId();
    }
}