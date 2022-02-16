package com.cema.health.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "illness")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaIllness {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id")
    private UUID id;

    @Column(name = "starting_date")
    private Date startingDate;

    @Column(name = "ending_date")
    private Date endingDate;

    @Column(name = "bovine_tag")
    private String bovineTag;

    @Column(name = "establishment_cuig")
    private String establishmentCuig;

    @Column(name = "worker_username")
    private String workerUsername;

    @ManyToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "disease_id")
    private CemaDisease disease;

    @OneToMany(mappedBy = "illness", cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CemaNote> cemaNotes = new ArrayList<>();

    @Override
    public String toString() {
        return "CemaIllness{" +
                "id=" + id +
                ", startingDate=" + startingDate +
                ", endingDate=" + endingDate +
                ", bovineTag='" + bovineTag + '\'' +
                ", establishmentCuig='" + establishmentCuig + '\'' +
                ", disease=" + disease +
                ", cemaNotes=" + cemaNotes +
                '}';
    }
}
