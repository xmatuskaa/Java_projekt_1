package cz.mendelu.ja.leteckaposta.country;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
class CountryService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${restcountries.url}")
    private String url;

    @Data
    private static class Neighbour {
        private final String country_cca3;
        private final String neighbour_cca3;
    }

    @Data
    private static class ForignCountry {
        private String cca3;
        private String[] borders;
        private String[] capital;
        private double[] latlng;
        private Map capitalInfo;

        public Stream<Neighbour> getNeighbours() {
            return (Objects.isNull(borders)) 
                ? Stream.empty() 
                : Arrays.stream(borders).map(neighbour -> new Neighbour(cca3, neighbour));
        }
    }

    @Transactional
    void deleteAllCountries(){
        log.info("Delete all countries.");
        jdbcTemplate.execute("delete from boarders");
        jdbcTemplate.execute("delete from countries");
    }

    @Transactional
    List<ForignCountry> fetchCountries(String region){
        log.info("Update countries fo region: {}{}", url, region);
        var restTemplate = new RestTemplate();
        var response = restTemplate.getForEntity(url + region, ForignCountry[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Connection fo region {} failed. Response code: {}.", region, response.getStatusCode());
            throw new IllegalArgumentException(MessageFormat.format("Connection fo region {} failed. Response code: {}.", region, response.getStatusCode()));
        }
        var countries = Arrays.asList(response.getBody());

        var countCountries = jdbcTemplate.batchUpdate(
            "insert into countries (cca3, capital, capital_lat, capital_lng) values (?, ?, ?, ?)", 
            countries, 
            100,
            (ps, country) -> {
                log.debug("Create county {}", country);
                var c = (ForignCountry) country;
                ps.setString(1, c.cca3);
                ps.setString(2, c.capital[0]);
                ps.setDouble(3, c.latlng[0]);
                ps.setDouble(4, c.latlng[1]);
            }
        );

        jdbcTemplate.batchUpdate(
            "insert into boarders (country_cca3, neighbour_cca3) values (?, ?)",
            countries.stream().flatMap(ForignCountry::getNeighbours).toList(),
            100,
            (ps, neighbour) -> {
                log.trace("Create border {}", neighbour);
                ps.setString(1, neighbour.country_cca3);
                ps.setString(2, neighbour.neighbour_cca3);
            }
        );

        return countries;
    }

}
