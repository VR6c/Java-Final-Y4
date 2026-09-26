package com.example.demo.service.school.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.StudentPatchRequest;
import com.example.demo.dto.request.school.StudentRequest;
import com.example.demo.dto.response.school.StudentResponse;
import com.example.demo.entity.school.Card;
import com.example.demo.entity.school.Major;
import com.example.demo.entity.school.Student;
import com.example.demo.entity.school.Subject;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.school.CardRepository;
import com.example.demo.repository.school.MajorRepository;
import com.example.demo.repository.school.StudentRepository;
import com.example.demo.repository.school.SubjectRepository;
import com.example.demo.service.school.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;
    private final CardRepository cardRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    public StudentServiceImpl(StudentRepository studentRepository,
                              MajorRepository majorRepository,
                              CardRepository cardRepository,
                              SubjectRepository subjectRepository,
                              ModelMapper modelMapper) {
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
        this.cardRepository = cardRepository;
        this.subjectRepository = subjectRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<StudentResponse> getAll(Pageable pageable) {
        return studentRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public StudentResponse create(StudentRequest request) {
        Student student = new Student();
        student.setName(request.name());
        student.setAge(request.age());

        if (request.majorId() != null) {
            student.setMajor(findMajorById(request.majorId()));
        }
        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            student.setSubjects(fetchAndValidateSubjects(request.subjectIds()));
        }

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_STUDENTS, key = "#id")
    public StudentResponse getById(Long id) {
        return toResponse(findStudentById(id));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_STUDENTS, key = "#id")
    public StudentResponse update(Long id, StudentRequest input) {
        Student existingStudent = findStudentById(id);
        existingStudent.setName(input.name());
        existingStudent.setAge(input.age());

        Major major = input.majorId() != null ? findMajorById(input.majorId()) : null;
        existingStudent.setMajor(major);

        if (input.subjectIds() != null && !input.subjectIds().isEmpty()) {
            existingStudent.setSubjects(fetchAndValidateSubjects(input.subjectIds()));
        } else {
            existingStudent.getSubjects().clear();
        }

        if (existingStudent.getCard() != null) {
            syncCardNumber(existingStudent.getCard(), major, existingStudent.getId());
        }

        return toResponse(studentRepository.save(existingStudent));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_STUDENTS, key = "#id")
    public StudentResponse edit(Long id, StudentPatchRequest input) {
        Student existingStudent = findStudentById(id);

        if (input.name() != null) {
            existingStudent.setName(input.name());
        }
        if (input.age() != null) {
            existingStudent.setAge(input.age());
        }
        if (Boolean.TRUE.equals(input.unlinkMajor())) {
            existingStudent.setMajor(null);
            if (existingStudent.getCard() != null) {
                syncCardNumber(existingStudent.getCard(), null, existingStudent.getId());
            }
        } else if (input.majorId() != null) {
            Major major = findMajorById(input.majorId());
            existingStudent.setMajor(major);
            if (existingStudent.getCard() != null) {
                syncCardNumber(existingStudent.getCard(), major, existingStudent.getId());
            }
        }

        if (Boolean.TRUE.equals(input.unlinkSubjects())) {
            existingStudent.getSubjects().clear();
        } else if (input.subjectIds() != null) {
            existingStudent.setSubjects(fetchAndValidateSubjects(input.subjectIds()));
        }

        return toResponse(studentRepository.save(existingStudent));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_STUDENTS, key = "#id")
    public void deleteById(Long id) {
        studentRepository.delete(findStudentById(id));
    }

    @Override
    public Page<StudentResponse> search(String name, Integer minAge, Integer maxAge, Pageable pageable) {
        return studentRepository.searchStudents(name, minAge, maxAge, pageable)
                .map(this::toResponse);
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Major findMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with id: " + id));
    }

    private List<Subject> fetchAndValidateSubjects(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> distinctIds = subjectIds.stream().distinct().toList();
        List<Subject> foundSubjects = subjectRepository.findAllById(distinctIds);
        if (foundSubjects.size() != distinctIds.size()) {
            List<Long> foundIds = foundSubjects.stream().map(Subject::getId).toList();
            List<Long> missingIds = distinctIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Subject not found with id: " + missingIds.getFirst());
        }
        return foundSubjects;
    }

    private void syncCardNumber(Card card, Major major, Long studentId) {
        String majorName = (major != null) ? major.getName() : null;
        card.setCardNumber(AppConstants.formatCardNumber(majorName, studentId));
        cardRepository.save(card);
    }

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        StudentResponse response = modelMapper.map(student, StudentResponse.class);

        if (student.getMajor() != null) {
            response.setMajorId(student.getMajor().getId());
            response.setMajorName(student.getMajor().getName());
        }

        if (student.getSubjects() != null && !student.getSubjects().isEmpty()) {
            response.setSubjectIds(student.getSubjects().stream().map(Subject::getId).toList());
            response.setSubjectNames(student.getSubjects().stream().map(Subject::getName).toList());
        } else {
            response.setSubjectIds(Collections.emptyList());
            response.setSubjectNames(Collections.emptyList());
        }

        if (student.getCard() != null) {
            Card card = student.getCard();
            response.setCardId(card.getId());
            response.setIssueDate(card.getIssueDate());
            if (student.getMajor() != null && student.getMajor().getName() != null && !student.getMajor().getName().trim().isEmpty()) {
                response.setCardNumber(AppConstants.formatCardNumber(student.getMajor().getName(), student.getId()));
            } else if (card.getCardNumber() != null && !card.getCardNumber().trim().isEmpty()) {
                response.setCardNumber(card.getCardNumber().trim());
            } else {
                response.setCardNumber(AppConstants.formatDefaultCardNumber(student.getId()));
            }
        }

        return response;
    }
}
