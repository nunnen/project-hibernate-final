package domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "country_language", schema = "world")
public class CountryLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    @Column(nullable = false, length = 30)
    String language;

    @Column(name = "is_official", nullable = false)
    Byte isOfficial;

    @Column(nullable = false)
    BigDecimal percentage;

}
