package com.cema.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @ApiModelProperty(notes = "The date when this sickness was detected", example = "2021-02-12 00:14:00")
    @NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "America/Buenos_Aires")
    private Date startingDate;
    @ApiModelProperty(notes = "The date when this sickness is estimated to end", example = "2021-03-12 00:14:00")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "America/Buenos_Aires")
    @NotNull
    private Date endingDate;
    @ApiModelProperty(notes = "Observation on the progression of this illness")
    private List<Note> notes;
}
