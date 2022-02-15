package com.cema.health.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "disease")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaDisease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "establishment_cuig")
    private String establishmentCuig;

    @Column(name = "description")
    private String description;

    @Column(name = "duration")
    private Long duration;

    @OneToMany(mappedBy = "disease", cascade = { CascadeType.DETACH }, fetch = FetchType.LAZY)
    private List<CemaIllness> cemaIllness;

    @Override
    public String toString() {
        return "CemaDisease{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", establishmentCuig='" + establishmentCuig + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                '}';
    }
}
