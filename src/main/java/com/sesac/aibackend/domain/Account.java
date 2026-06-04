package com.sesac.aibackend.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    private String accountNumber;
    private String accountHolder;
    private Long balance;

    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

}
