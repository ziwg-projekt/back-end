package pl.ziwg.backend.externalapi.opencagedata;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;
import pl.ziwg.backend.externalapi.opencagedata.entity.OpencageRate;

import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
public class GeocodeRepositoryImpl implements GeocodeRepository
{
    private static final String API_KEY = "key";

    private static final String QUERY = "q";

    @Getter
    @Setter
    private String apiKey;

    @Getter
    @Setter
    private String urlBase = "https://api.opencagedata.com/geocode/v1/json?";

    @Getter
    @Setter
    private RestOperations template;

    private static final OpencageRate rate = new OpencageRate();

    @Getter
    private LocalDateTime reset = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    public GeocodeRepositoryImpl(String apiKey)
    {
        this.template = new RestTemplate();
        this.apiKey = apiKey;
    }

    public int getLimit()
    {
        return rate.getLimit();
    }

    public int getRemaining()
    {
        return rate.getRemaining();
    }

    public GeocodeResponse query(String query)
    {
        URI serviceUrl = UriComponentsBuilder.fromUriString(getUrlBase())
                .queryParam(API_KEY, apiKey)
                .queryParam(QUERY, query)
                .build()
                .encode()
                .toUri();

        log.debug("geocoding query: {}", serviceUrl);
        GeocodeResponse result = new GeocodeResponse();

        try
        {
            ResponseEntity<GeocodeResponse> response = template.getForEntity(serviceUrl, GeocodeResponse.class);

            switch (response.getStatusCode())
            {
                case OK:
                    result = response.getBody();

                    if (result != null)
                    {
                        if (result.getRate() != null)
                        {
                            rate.setLimit(result.getRate().getLimit());
                            rate.setRemaining(result.getRate().getRemaining());
                            rate.setReset(result.getRate().getReset());

                            reset = LocalDateTime.ofEpochSecond(result.getRate().getReset(), 0, ZoneOffset.UTC);
                        }
                    }
                    break;

                default:
                    break;
            }

            log.debug("  {} of {} queries remaining", rate.getRemaining(), rate.getLimit());
            log.debug("  limit will be reset at {}, now it is {}", reset, LocalDateTime.now());
        }
        catch (RestClientException restClientException)
        {
            log.error("While calling API", restClientException);
        }

        return result;
    }

    public GeocodeResponse reverse(double latitude, double longitude)
    {
        log.debug("reverse geocoding {}, {}", latitude, longitude);

        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);

        String query = df.format(latitude) + "+" + df.format(longitude);

        log.info("query is '{}'", query);

        return query(query);
    }
}
