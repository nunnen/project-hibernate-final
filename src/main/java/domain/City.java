package domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "city", schema = "world")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String name;

    @ManyToOne()
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(length = 20, nullable = false)
    private String district;

    @Column(nullable = false)
    private Integer population;
}
