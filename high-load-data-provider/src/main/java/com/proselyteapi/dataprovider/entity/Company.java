package com.proselyteapi.dataprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

//@RedisHash
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("company")
public class Company implements Serializable {
    @Id
    private Long id;
    private String name;
    private boolean enabled;
    private String symbol;
    @Transient
    private List<Stock> stocks;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}