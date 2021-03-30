package pl.ziwg.backend.configuration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.externalapi.governmentapi.PersonRegister;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.model.repository.*;
import pl.ziwg.backend.service.*;

import java.security.SecureRandom;
import java.util.*;

@Component
public class AfterStartupConfiguration {
    protected static final Logger log = Logger.getLogger(BackendApplication.class);
    private static final SecureRandom random = new SecureRandom();
    private String hospitalUsername = "first_hospital";

    private UserService userService;
    private RoleService roleService;
    private CompanyService companyService;
    private PersonRegister personRegister;
    private DoctorService doctorService;
    private VaccineService vaccineService;
    private AppointmentService appointmentService;


    @Autowired
    public AfterStartupConfiguration(UserService userService,
                                     RoleService roleService,
                                     CompanyService companyService,
                                     PersonRegister personRegister,
                                     DoctorService doctorService,
                                     VaccineService vaccineService,
                                     AppointmentService appointmentService){
        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
        this.personRegister = personRegister;
        this.doctorService = doctorService;
        this.vaccineService = vaccineService;
        this.appointmentService = appointmentService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData(){
        createRolesIfNotExist();
        createAdminIfNotExists();
        createCompanies();
        createCitizen();
        createHospital();
        createDoctors();
        createVaccines();
        createAppointment();
    }

    private void createVaccines(){
        List<String> codes = new ArrayList<>(Arrays.asList("2342", "2455", "2445", "4676", "24421", "35424", "13", "2445"));
        List<Company> companies = companyService.findAll();
        Optional<User> user = userService.findByUsername(hospitalUsername);
        if(user.isPresent()) {
            Hospital hospital = user.get().getHospital();
            if (hospital == null) {
                log.warn("Hospital doesn't exists in system!");
            }
            else if(hospital.getVaccines().isEmpty()){
                for(String code : codes){
                    Company company = companies.get(random.nextInt(companies.size()));
                    Vaccine vaccine = new Vaccine(code, company, hospital);
                    vaccine.setState(randomEnum(VaccineState.class));
                    vaccineService.save(vaccine);
                    log.info("Add vaccine with code '" + code + "' for hospital!");
                }
            }
            else{
                log.info("No vaccines where added, hospital has got some vaccines!");
            }
        } else{
            log.warn("User hospital doesn't exists in system!");
        }
    }

    private static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private void createAppointment(){
        //TODO: create exemplary appointment
    }


    private void createCitizen(){
        String username = "first_user";
        String password = "password";
        String pesel = "2888924742";
        if(userService.checkIfUserExists(username)){
            log.info("User (type citizen) '" + username + "' was found in system");
        }
        else {
            Address address = new Address("Krakow", "Kazimierza Wielkiego", "32");
            userService.saveCitizen(username, password, new Citizen(personRegister.getPersonByPeselMock(pesel), address));
        }
    }

    private void createHospital(){
        String password = "password";
        if(userService.checkIfUserExists(hospitalUsername)){
            log.info("User (type hospital) '" + hospitalUsername + "' was found in system");
        }
        else {
            Address address = new Address("Wroclaw", "Nowowiejska", "13");
            userService.saveHospital(hospitalUsername, password,  new Hospital("Szpital na Nowowiejskiej", address));
        }
    }

    private void createDoctors(){
        Optional<User> user = userService.findByUsername(hospitalUsername);
        if(user.isPresent()) {
            Hospital hospital = userService.findByUsername(hospitalUsername).get().getHospital();
            if(hospital==null){
                log.warn("Hospital doesn't exists in system!");
            }
            else if (hospital.getDoctors().isEmpty()) {
                doctorService.save(new Doctor(hospital));
                log.info("Add doctor for hospital!");
            } else {
                log.info("No doctor was added, hospital has got some doctors!");
            }
        }
        else{
            log.warn("User hospital doesn't exists in system!");
        }
    }

    private void createAdminIfNotExists() {
        String password = "adminpassword";
        String username = "admin";
        if(userService.checkIfExistsUserWithGivenAuthorities(Collections.singletonList(roleService.findByName(RoleName.ROLE_ADMIN).get()))){
            log.info("Admin was found in system");
        }
        else{
            userService.saveAdmin(username, password);
        }
    }

    private void createCompanies(){
        List<String> companies = new ArrayList<>(Arrays.asList("Pfizer", "AstraZeneca", "Johnson&Johnson"));
        for(String company : companies){
            companyService.addIfNotExists(company);
        }
    }


    private void createRolesIfNotExist(){
        for (RoleName roleName : RoleName.values()) {
            roleService.addIfNotExists(roleName);
        }
    }


}
