package cz.mendelu.ja.leteckaposta.plane;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name="planes")
@Entity
public class Plane {
    @Id
    private String code;
    private double carryingCapacity;
    private double flyDistance;

    private String defaultLocation;

    public Plane(String code, double carryingCapacity, double flyDistance, String defaultLocation) {

        this.code = code;
        this.carryingCapacity = carryingCapacity;
        this.flyDistance = flyDistance;
        this.defaultLocation = defaultLocation;
    }

    public Plane() {

    }
}
