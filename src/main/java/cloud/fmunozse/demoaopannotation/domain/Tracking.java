package cloud.fmunozse.demoaopannotation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tracking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @CreatedDate
    @Column(updatable = false)
    Date createdAt;

    @Column(length = 511)
    String urlRequest;

    @Column(length = 10)
    String method;

    @Column(length = 255)
    String hashRequest;

    @Lob
    String inputJson;

    @Lob
    String outputJson;

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }
}
