package com.studyolle.modules.event;


import com.studyolle.modules.account.Account;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter @EqualsAndHashCode(of = "id")
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;
}
