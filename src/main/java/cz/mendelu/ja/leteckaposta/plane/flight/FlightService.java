package cz.mendelu.ja.leteckaposta.plane.flight;

import cz.mendelu.ja.leteckaposta.country.CountryService;
import cz.mendelu.ja.leteckaposta.parcel.Parcel;
import cz.mendelu.ja.leteckaposta.parcel.ParcelRepository;
import cz.mendelu.ja.leteckaposta.plane.Plane;
import cz.mendelu.ja.leteckaposta.plane.PlaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final PlaneRepository planeRepository;
    private final ParcelRepository parcelRepository;
    private final CountryService countryService;
    @Autowired
    public FlightService(FlightRepository flightRepository, PlaneRepository planeRepository, ParcelRepository parcelRepository,CountryService countryService){
        this.flightRepository = flightRepository;
        this.planeRepository = planeRepository;
        this.parcelRepository = parcelRepository;
        this.countryService = countryService;
    }
    @Transactional
    public void addFlight(String parcelId){
        Parcel p = parcelRepository.getReferenceById(parcelId);
        String parcelDepatrure = p.getLocation();
        Double parcelDepartureLat = countryService.getCountryLat(parcelDepatrure);
        Double parcelDepartureLon = countryService.getCountryLon(parcelDepatrure);
        List<Plane> availablePlanes = planeRepository.findAll();
        String closestPlane;
        double minDistance=999999999;
        Plane minPlane = null;
        for (Plane plane: availablePlanes) {
            Double planeDepartureLat = countryService.getCountryLat(plane.getCurrentLocation());
            Double planelDepartureLon = countryService.getCountryLon(plane.getCurrentLocation());
            double distance = calculateDistanceInMeters(parcelDepartureLat, parcelDepartureLon, planeDepartureLat, planelDepartureLon);
            if (distance < minDistance) {
                minDistance = distance;
                minPlane = plane;
            }
        }
        flightRepository.save(new Flight(minPlane.getCode().toString(),minPlane.getCode().toString()+parcelId,p.getLocation(),p.getDestination()));
        minPlane.setCurrentLocation(p.getDestination());
       XMLgenerator.updateXML(p.getLocation(),p.getDestination(),p.getId(), LocalDateTime.now());



    }
    public double calculateDistanceInMeters(double lat1, double long1, double lat2,
                                            double long2) {


        double dist = org.apache.lucene.util.SloppyMath.haversinMeters(lat1, long1, lat2, long2);
        return dist;
    }
}

