package pl.ziwg.backend.algorithm;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.service.CitizenService;
import pl.ziwg.backend.service.HospitalService;
import pl.ziwg.backend.service.VaccineService;

import java.util.List;

@Component
public class AssignmentAlgorithm implements Runnable, DisposableBean {
    protected static final Logger log = Logger.getLogger(AssignmentAlgorithm.class);
    private volatile boolean active = true;  // set to 'true' to run algorithm
    private Thread thread;
    private VaccineService vaccineService;
    private CitizenService citizenService;
    private HospitalService hospitalService;


    @Autowired
    AssignmentAlgorithm(VaccineService vaccineService, CitizenService citizenService, HospitalService hospitalService){
        this.thread = new Thread(this);
        this.thread.start();
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
        this.hospitalService = hospitalService;
    }

    @SneakyThrows
    @Override
    public void run() {
        while(active){
            Thread.sleep(2000);
            for(Hospital hospital : hospitalService.findAll()){
//                log.info("Looking for available vaccines in hospital - " + hospital.getName());
                List<Vaccine> vaccines = vaccineService.findAllAvailableFromHospital(hospital);
//                log.info(vaccines.size() + " available vaccines.");
            }
            //TODO: add checking how many citizen wait for vaccine

        }
    }

    @Override
    public void destroy() {
        active = false;
    }
}
