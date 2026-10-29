package com.example.outage.config;

import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import com.example.outage.repository.OutageReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with realistic sample outage records for development and demonstration.
 *
 * <p><strong>NOTE: ALL DATA BELOW IS SAMPLE DATA FOR DEMONSTRATION ONLY.</strong>
 * Not based on real incidents. Active only when NOT running with the "prod" Spring profile.
 */
@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final OutageReportRepository repository;

    public DataInitializer(OutageReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Database already has {} report(s). Skipping sample data seed.", repository.count());
            return;
        }

        log.info("Seeding sample outage reports (DEMONSTRATION DATA ONLY)...");

        repository.save(OutageReport.builder()
                .title("Power outage in MG Road area")
                .location("MG Road, Sector 5, Nashik")
                .description("Complete power failure affecting approximately 200 households. " +
                             "Outage started around 6:30 AM. Transformers are making unusual sounds.")
                .reporterName("Ramesh Kulkarni")
                .status(OutageStatus.REPORTED)
                .build());

        repository.save(OutageReport.builder()
                .title("Flickering lights and voltage fluctuation")
                .location("Peth Road, Camp Area, Pune")
                .description("Voltage fluctuations causing damage to electrical appliances. " +
                             "Multiple residents on the street have reported the same issue " +
                             "since yesterday evening.")
                .reporterName("Sunita Desai")
                .status(OutageStatus.IN_PROGRESS)
                .build());

        repository.save(OutageReport.builder()
                .title("Broken street light on highway junction")
                .location("Mumbai-Pune Highway, Khopoli Junction")
                .description("Three consecutive street lights are non-functional near the " +
                             "highway junction. Poses a safety risk especially at night. " +
                             "Traffic is heavy in this area.")
                .reporterName("Arun Patil")
                .status(OutageStatus.REPORTED)
                .build());

        repository.save(OutageReport.builder()
                .title("Transformer failure — residential colony")
                .location("New Colony, Aurangabad")
                .description("Transformer serving Block C and D of New Colony tripped at " +
                             "11:45 PM last night. About 150 flats are without power. " +
                             "Generator backup is running but will last only 4 more hours.")
                .reporterName("Meena Joshi")
                .status(OutageStatus.IN_PROGRESS)
                .build());

        repository.save(OutageReport.builder()
                .title("Power restored after 8-hour outage")
                .location("Shivaji Nagar, Solapur")
                .description("Power was restored at 4:00 PM after an 8-hour outage caused by " +
                             "a tripped feeder line. All households in the locality have power again.")
                .reporterName("Vijay Sharma")
                .status(OutageStatus.RESOLVED)
                .build());

        repository.save(OutageReport.builder()
                .title("Underground cable damage due to road work")
                .location("Station Road, Near Bus Stand, Nagpur")
                .description("Road digging work by civic contractors accidentally cut an " +
                             "underground electricity cable. Affected 2 commercial establishments " +
                             "and 30 residences.")
                .reporterName("Priya Bhandari")
                .status(OutageStatus.RESOLVED)
                .build());

        repository.save(OutageReport.builder()
                .title("Overhead wire snapped during storm")
                .location("Parbhani Market Road, Parbhani")
                .description("Heavy rain and strong winds last night caused an overhead high-tension " +
                             "wire to snap and fall on the road. Area is currently cordoned off. " +
                             "Power is cut to prevent electrocution risk.")
                .reporterName("Santosh Gaikwad")
                .status(OutageStatus.REPORTED)
                .build());

        log.info("Seeded {} sample outage reports. (DEMONSTRATION DATA — NOT REAL INCIDENTS)",
                repository.count());
    }
}
