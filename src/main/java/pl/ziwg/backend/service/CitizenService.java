package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.repository.CitizenRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class CitizenService {
    private CitizenRepository citizenRepository;

    @Autowired
    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    public Page<Citizen> findAllFromPage(Pageable pageable){
        return citizenRepository.findAll(pageable);
    }

    public List<Citizen> findAll(){
        return citizenRepository.findAll();
    }

    public Optional<Citizen> findByPesel(String pesel) {
        return citizenRepository.findByPesel(pesel);
    }

    public Citizen save(Citizen citizen) {
        return citizenRepository.save(citizen);
    }

    public void deleteCitizen(String pesel){
        citizenRepository.deleteByPesel(pesel);
    }

    public boolean checkIfExistsByPesel(String pesel){
        return citizenRepository.existsByPesel(pesel);
    }

}
