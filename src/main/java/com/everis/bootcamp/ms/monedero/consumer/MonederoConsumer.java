package com.everis.bootcamp.ms.monedero.consumer;

import com.everis.bootcamp.ms.monedero.domain.dto.MonederoTransactionDto;
import com.everis.bootcamp.ms.monedero.domain.entity.MonederoDebitCard;
import com.everis.bootcamp.ms.monedero.producer.MonederoProducer;
import com.everis.bootcamp.ms.monedero.service.srv.MonederoAccountService;
import com.everis.bootcamp.ms.monedero.service.srv.MonederoDebitCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonederoConsumer {

    @Autowired
    private MonederoAccountService service;

    @Autowired
    private MonederoDebitCardService monederoDebitCard;



    @Autowired
    private MonederoProducer producer;

    @KafkaListener(
            topics = "${custom.kafka.topic-name}",
            groupId = "${custom.kafka.group-id}",
            containerFactory = "monederoKafkaListenerContainerFactory")
    public void consumer(MonederoTransactionDto monederoTransactionDto) {

        log.info("Mensaje consumido [{}]", monederoTransactionDto);
        this.validarBalance(monederoTransactionDto);
    }

    private void validarBalance(MonederoTransactionDto monederoTransactionDto) {

        service.findByNroPhone(monederoTransactionDto.getOriginNumber())
                .flatMap(m -> {

                    if (m.getBootcoin() >= monederoTransactionDto.getBootcoin()) {

                        Double nuevoBalance = m.getBootcoin() - monederoTransactionDto.getBootcoin();
                        m.setBootcoin(nuevoBalance);
                        service.getMonedero(m).flatMap(mObj -> service.save(mObj))
                                .flatMap(d -> service.findByNroPhone(monederoTransactionDto.getDestinationNumber()))
                                .flatMap(md -> service.getMonedero(md).flatMap(mde -> {
                                    mde.setBootcoin(mde.getBootcoin() + monederoTransactionDto.getBootcoin());
                                    return service.save(mde);
                                })).subscribe(c -> log.info("ACTUALIZADO TODO"));

                        monederoTransactionDto.setStatus(MonederoTransactionDto.Status.SUCCESSFUL);

                    } else {

                        //REVISAR SI TIENE DEBITO ASOCIADA
                        monederoDebitCard.findByNroPhone(monederoTransactionDto.getOriginNumber()).flatMap(mdc -> {
                            log.info("getCardNumber: "+ mdc.getCardNumber());
                                return monederoDebitCard.getDebitCardAccount(mdc);
                            }
                        );

                        // SI NO TIENE -> RECHAZAR PETICION
                        monederoTransactionDto.setStatus(MonederoTransactionDto.Status.REJECTED);
                    }
                    this.producer.producer(monederoTransactionDto);
                    return service.getMonedero(m);
                }).subscribe();
    }
}
