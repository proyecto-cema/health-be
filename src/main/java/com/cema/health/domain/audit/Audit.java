package com.cema.health.domain.audit;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    @ApiModelProperty(notes = "The status of the response", example = "201")
    private String responseStatus;
    @ApiModelProperty(notes = "The request sent by the user")
    private String requestBody;
    @ApiModelProperty(notes = "The response returned by the system")
    private String responseBody;
    @ApiModelProperty(notes = "The ip address of the client")
    private String localAddress;
    @ApiModelProperty(notes = "The headers sent by the user")
    private String requestHeaders;
    @ApiModelProperty(notes = "The uri hit by the user")
    private String uri;
    @ApiModelProperty(notes = "The http method used by the user", example = "PUT")
    private String httpMethod;
    @ApiModelProperty(notes = "The method name for the controller entrypoint", example = "lookUpBovineByTag")
    private String method;
    @ApiModelProperty(notes = "The role of the user", example = "PATRON")
    private String role;
    @ApiModelProperty(notes = "The username of the user", example = "merlinds")
    private String username;
    @ApiModelProperty(notes = "The cuig of the user", example = "123")
    private String establishmentCuig;
    @ApiModelProperty(notes = "The moment when the action was executed")
    private Date auditDate;
    @ApiModelProperty(notes = "The module this action was executed in", example = "bovine")
    private String module;
}
