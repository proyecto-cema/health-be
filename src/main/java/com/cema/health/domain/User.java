package com.cema.health.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @ApiModelProperty(notes = "The cuig of the establishment this user belongs to", example = "312")
    @NotEmpty(message = "Establishment is required")
    private String establishmentCuig;
    @ApiModelProperty(notes = "The username for this user, needed for login", example = "merlinds")
    @NotEmpty(message = "Username is required")
    private String userName;
    @ApiModelProperty(notes = "The name of this user", example = "Merlin")
    @NotEmpty(message = "Name is required")
    private String name;
    @ApiModelProperty(notes = "The last name of this user", example = "Nuñez")
    @NotEmpty(message = "Last name is required")
    private String lastName;
    @ApiModelProperty(notes = "The phone number of this user", example = "3541330188")
    private String phone;
    @ApiModelProperty(notes = "The email address of this user", example = "merlinsn@gmail.com")
    private String email;
    @ApiModelProperty(notes = "The role of this user", example = "admin")
    @NotEmpty(message = "Role name is required")
    @Pattern(regexp = "(?i)admin|peon|patron")
    private String role;
    @ApiModelProperty(notes = "When was this user created", hidden = true)
    private Date creationDate;
}
