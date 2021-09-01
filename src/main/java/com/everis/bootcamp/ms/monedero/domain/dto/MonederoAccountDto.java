package com.everis.bootcamp.ms.monedero.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MonederoAccountDto {

    private String id;

    private String typeDocument;

    private String nroDocument;

    private int nroPhone;

    private String email;

    private Double bootcoin;

    private String cardNumber;

    private LocalDateTime createAt;

}
