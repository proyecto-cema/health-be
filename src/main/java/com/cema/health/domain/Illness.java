package com.cema.health.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Illness {
    @ApiModelProperty(notes = "The id to identify this Illness", example = "b000bba4-229e-4b59-8548-1c26508e459c")
    private UUID id;
    @ApiModelProperty(notes = "The name of the disease causing this illness", example = "Aftosa")
    @NotEmpty(message = "Disease name is required")
    private String diseaseName;
    @ApiModelProperty(notes = "The bovine affected by this illness", example = "12223")
    private String bovineTag;
    @ApiModelProperty(notes = "The cuig this illness is related to", example = "123")
    @NotEmpty(message = "establishmentCuig is required")
    private String establishmentCuig;
    @ApiModelProperty(notes = "The date when this sickness was detected", example = "2021-02-12")
    @NotNull
    private Date startingDate;
    @ApiModelProperty(notes = "The date when this sickness is estimated to end", example = "2021-03-12")
    @NotNull
    private Date endingDate;
    @ApiModelProperty(notes = "Observation on the progression of this illness")
    private List<Note> notes;
}
