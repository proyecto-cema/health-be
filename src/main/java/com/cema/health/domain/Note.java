package com.cema.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Note {
    @ApiModelProperty(notes = "Details by the user")
    private String content;
    @ApiModelProperty(notes = "When this note was created")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date creationDate;
}
