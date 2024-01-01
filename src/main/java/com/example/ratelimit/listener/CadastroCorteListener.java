package com.example.ratelimit.listener;



import com.example.ratelimit.listener.data.CadastroMessage;
import com.example.ratelimit.ratelimiter.RateLimiter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CadastroCorteListener {

    private final ObjectMapper mapper;
    private final RateLimiter rateLimiter;
    private final Counter counter;

    public CadastroCorteListener(ObjectMapper mapper, RateLimiter rateLimiter, CollectorRegistry collectorRegistry) {
        this.mapper = mapper;
        this.rateLimiter = rateLimiter;
        counter = Counter.build()
                .name("messages")
                .help("Number of messages")
                .labelNames("tipo")
                .register(collectorRegistry);

    }

    @SqsListener(value = "${cloud.aws.queue.name}")
    public void onMessage(@Payload String rawMessage) {
        try {
            log.info("Mensagem recebida {}", rawMessage);
            counter.labels("total").inc();

            CadastroMessage message = mapper.readValue(rawMessage, CadastroMessage.class);

            String rateLimitKey = genKey(message);

            if (rateLimiter.tryConsume(rateLimitKey)) {
                process(message);
                log.info("Mensagem processada com sucesso");
                counter.labels("sucesso").inc();
            } else {
                log.info("Rate limit estorou limite permitido");
                counter.labels("rate_limit_exceeded").inc();
                throw new RuntimeException("Rate Limit exceeded");
            }

        } catch (JsonProcessingException e) {
            log.error("Error ao processar mensagem");
            counter.labels("error").inc();
        }
    }

    public void process(CadastroMessage message){
        log.info("Processando mensagem " + message.hashCode());
    }

    public String genKey(CadastroMessage message){
        return message.getCpf() + "#" + message.getNomeCliente();
    }



}
