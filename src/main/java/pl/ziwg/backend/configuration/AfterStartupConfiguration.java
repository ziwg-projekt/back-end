package pl.ziwg.backend.configuration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.externalapi.governmentapi.PersonRegister;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.VaccineState;
import org.springframework.mock.web.MockMultipartFile;
import pl.ziwg.backend.service.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private FileStorageService fileStorageService;

    @Autowired
    public AfterStartupConfiguration(UserService userService,
                                     RoleService roleService,
                                     CompanyService companyService,
                                     PersonRegister personRegister,
                                     DoctorService doctorService,
                                     VaccineService vaccineService,
                                     AppointmentService appointmentService,
                                     FileStorageService fileStorageService){
        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
        this.personRegister = personRegister;
        this.doctorService = doctorService;
        this.vaccineService = vaccineService;
        this.appointmentService = appointmentService;
        this.fileStorageService = fileStorageService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() throws URISyntaxException, IOException {
        createRolesIfNotExist();
        createAdminIfNotExists();
        createCompaniesWithLogos();
        createCitizen();
        createHospital();
        createDoctors();
        createVaccines();
        createAppointment();
    }

    private void createVaccines(){
        List<String> codes = new ArrayList<>(Arrays.asList("2342", "2455", "2445", "4676", "24421", "35424", "13", "2446"));
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

    private void createCompaniesWithLogos() throws URISyntaxException, IOException {
        List<String> companies = new ArrayList<>(Arrays.asList("Pfizer", "AstraZeneca", "Johnson&Johnson"));
        for(String companyName : companies){
            Company company = companyService.addIfNotExists(companyName);
            File file = ResourceUtils.getFile("classpath:" + companyName.toLowerCase() + ".png");
            InputStream in = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(companyName.toLowerCase() + ".png", companyName.toLowerCase() + ".png", "text/plain", in);
            String pathToFile = fileStorageService.storeFile(multipartFile);
            HttpServletRequest mockRequest = new MockHttpServletRequest();
            ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(mockRequest);
            RequestContextHolder.setRequestAttributes(servletRequestAttributes);
            String contextPath = ServletUriComponentsBuilder.fromRequest(mockRequest).toUriString();
            String fileDownloadUri = ServletUriComponentsBuilder.fromPath(contextPath)
                    .path(":8080")
                    .path("/api/v1/companies/logo/")
                    .path(pathToFile)
                    .toUriString();
            log.info("Logo for '" + companyName + "' saved in '" + fileDownloadUri + "'");
            company.setLogoPath(fileDownloadUri);
            companyService.save(company);
        }

    }


    private void createRolesIfNotExist(){
        for (RoleName roleName : RoleName.values()) {
            roleService.addIfNotExists(roleName);
        }
    }


}
