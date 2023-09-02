package com.proselyteapi.dataprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("stock")
public class Stock implements Serializable {
    @Id
    private Long id;
    @Indexed
    private String symbol;
    private Double price;
    @Column("is_privilege")
    private boolean isPrivilege = false;
    @Column("company_id")
    private long companyId;
    @Transient
    private Company company;
    @CreatedDate
    private LocalDateTime createdAt;
}