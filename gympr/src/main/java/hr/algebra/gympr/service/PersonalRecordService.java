package hr.algebra.gympr.service;

import hr.algebra.gympr.dto.PersonalRecordDto;
import hr.algebra.gympr.entity.PersonalRecord;
import hr.algebra.gympr.entity.User;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import hr.algebra.gympr.repository.PersonalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PersonalRecordService {

    private final PersonalRecordRepository recordRepository;

    public PersonalRecordService(PersonalRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public List<PersonalRecordDto> findAll() {
        return recordRepository.findAllByOrderByLiftDateDescCreatedAtDesc()
            .stream()
            .map(PersonalRecordDto::from)
            .toList();
    }

    public PersonalRecordDto findById(Long id) {
        return recordRepository.findById(id)
            .map(PersonalRecordDto::from)
            .orElseThrow(() -> new NoSuchElementException("Record not found: " + id));
    }

    public List<PersonalRecordDto> search(LiftType lift, MuscleGroup group, Boolean milestone, String query) {
        String nq = (query != null && query.isBlank()) ? null : query;
        return recordRepository.search(lift, group, milestone, nq)
            .stream()
            .map(PersonalRecordDto::from)
            .toList();
    }

    @Transactional
    public PersonalRecordDto create(PersonalRecordDto dto, User creator) {
        PersonalRecord record = new PersonalRecord();
        dto.applyTo(record);
        record.setAddedBy(creator);
        return PersonalRecordDto.from(recordRepository.save(record));
    }

    @Transactional
    public PersonalRecordDto update(Long id, PersonalRecordDto dto) {
        PersonalRecord record = recordRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Record not found: " + id));
        dto.applyTo(record);
        return PersonalRecordDto.from(recordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new NoSuchElementException("Record not found: " + id);
        }
        recordRepository.deleteById(id);
    }
}
