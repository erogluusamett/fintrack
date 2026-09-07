package com.fintrack;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Tüm entegrasyon testlerinin ortak temeli: H2 gibi sahte bir DB yerine
 * gerçek bir PostgreSQL container'ı kullanır — spec'in "Testcontainers ile
 * gerçek PostgreSQL ortamına yakın testler" isteğinin karşılığı.
 * <p>
 * Container, {@code @Testcontainers}/{@code @Container} KULLANMADAN, klasik
 * "singleton container" deseniyle yönetilir: static initializer JVM
 * başına yalnızca bir kez çalışır (Java sınıf yükleme garantisi), böylece
 * aynı Maven Surefire koşumunda birden fazla test sınıfı aynı container'ı
 * güvenle paylaşır. {@code @Testcontainers}'ın sınıf-bazlı otomatik
 * start/stop yaşam döngüsü bu paylaşımla çakışıp ikinci test sınıfında
 * container'ın yeniden oluşturulmasına (ve HikariCP havuzunun elindeki
 * bağlantıların sessizce geçersiz kalmasına) yol açıyordu — bu, JVM
 * kapanana kadar (Testcontainers'ın Ryuk'u temizler) hiç durdurulmayan tek
 * bir statik container ile çözüldü.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fintrack_test")
            .withUsername("fintrack_test")
            .withPassword("fintrack_test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Testler saniyeler içinde onlarca auth isteği atabiliyor; gerçek
        // rate limit değerleriyle test etmek RateLimiterService'in kendi
        // unit testinin işi, burada değil (bkz. RateLimiterService).
        registry.add("fintrack.rate-limit.auth-max-requests", () -> "10000");
    }
}
