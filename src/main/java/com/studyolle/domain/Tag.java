package com.studyolle.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.validator.constraints.Length;


@Builder
@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id") @NoArgsConstructor @AllArgsConstructor
public class Tag {

    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Length(min = 0, max = 20)
    @Column(unique = true, nullable = false)
    private String title;


}
