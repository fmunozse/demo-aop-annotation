package cloud.fmunozse.demoaopannotation.repository;

import cloud.fmunozse.demoaopannotation.domain.Tracking;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TrackingRepository  extends CrudRepository<Tracking,Long> {

    Optional<Tracking> findByHashRequest(String hashRequest);

}
