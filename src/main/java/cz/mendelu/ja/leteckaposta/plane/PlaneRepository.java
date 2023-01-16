package cz.mendelu.ja.leteckaposta.plane;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface PlaneRepository extends JpaRepository<Plane, String> {
}
