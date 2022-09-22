package com.afalenkin.webfluxnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class WebfluxNotesApplication {

    /**
     * BlockHound служит для обнаружения блокирующих вызовов в реактивных операциях.
     * Если BlockHound обнаружит такие операции - будет выброшено исключение.
     * В реактивном программировании нельзя чтобы вызовы были блокирующими - в таком случае нужно
     * предусматривать отдельные планировщики, которые будут выполнять такие операции.
     */
    static {
        BlockHound.install(
                // Для определенных операций таким образом можно разрешить блокирующие вызовы
                builder ->
                        builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(WebfluxNotesApplication.class, args);
    }

}
