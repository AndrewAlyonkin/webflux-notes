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
                                // после добавления зависимости от OpenAPI - blockHound стал блокировать работу
                                // приложения так как сваггер совершает синхронные блокирующие запросы для
                                // получения необходимой информации.
                                // Чтобы этого избежать - пришлось добавить такие исключения
                                .allowBlockingCallsInside("java.io.FilterInputStream", "read")
                                .allowBlockingCallsInside("java.io.InputStream", "readNBytes")
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(WebfluxNotesApplication.class, args);
    }

}
