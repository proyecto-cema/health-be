package com.cema.health.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "note")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "content")
    private String content;

    @ManyToOne(cascade = { CascadeType.REMOVE })
    @JoinColumn(name="illness_id")
    private CemaIllness illness;

    @Override
    public String toString() {
        return "CemaNote{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
