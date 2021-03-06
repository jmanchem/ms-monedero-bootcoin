package com.everis.bootcamp.ms.monedero.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Document("MonederoAccount")
public class MonederoAccount {

    @Id
    private String id;

    @NotEmpty
    private String typeDocument;

    @NotEmpty
    private String nroDocument;

    @NotNull
    private int nroPhone;

    @NotEmpty
    private String email;

    @NotNull
    private Double bootcoin;

    @NotNull
    private String cardNumber;

    private LocalDateTime createAt;

}

