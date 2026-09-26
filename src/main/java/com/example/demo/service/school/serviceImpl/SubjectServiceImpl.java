package com.example.demo.service.school.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.SubjectPatchRequest;
import com.example.demo.dto.request.school.SubjectRequest;
import com.example.demo.dto.response.school.StudentResponse;
import com.example.demo.dto.response.school.SubjectResponse;
import com.example.demo.entity.school.Card;
import com.example.demo.entity.school.Student;
import com.example.demo.entity.school.Subject;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.school.SubjectRepository;
import com.example.demo.service.school.SubjectService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    public SubjectServiceImpl(SubjectRepository subjectRepository, ModelMapper modelMapper) {
        this.subjectRepository = subjectRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<SubjectResponse> getAll(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_SUBJECTS, key = "#id")
    public SubjectResponse getById(Long id) {
        return toResponse(findSubjectById(id));
    }

    @Override
    public SubjectResponse create(SubjectRequest request) {
        String trimmedName = request.name() != null ? request.name().trim() : null;

        validateUniqueName(trimmedName, null);

        Subject subject = new Subject();
        subject.setName(trimmedName);
        subject.setDescription(request.description() != null ? request.description().trim() : null);

        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_SUBJECTS, key = "#id")
    public SubjectResponse update(Long id, SubjectRequest input) {
        Subject existingSubject = findSubjectById(id);

        String trimmedName = input.name() != null ? input.name().trim() : null;

        validateUniqueName(trimmedName, id);

        existingSubject.setName(trimmedName);
        existingSubject.setDescription(input.description() != null ? input.description().trim() : null);

        return toResponse(subjectRepository.save(existingSubject));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_SUBJECTS, key = "#id")
    public SubjectResponse edit(Long id, SubjectPatchRequest input) {
        Subject existingSubject = findSubjectById(id);

        if (input.name() != null) {
            String trimmedName = input.name().trim();
            validateUniqueName(trimmedName, id);
            existingSubject.setName(trimmedName);
        }
        if (input.description() != null) {
            existingSubject.setDescription(input.description().trim());
        }

        return toResponse(subjectRepository.save(existingSubject));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_SUBJECTS, key = "#id")
    public void deleteById(Long id) {
        Subject subject = findSubjectById(id);
        if (subject.getStudents() != null && !subject.getStudents().isEmpty()) {
            for (Student student : subject.getStudents()) {
                student.getSubjects().remove(subject);
            }
        }
        subjectRepository.delete(subject);
    }

    @Override
    public Page<SubjectResponse> search(String name, String description, Pageable pageable) {
        return subjectRepository.searchSubjects(name, description, pageable)
                .map(this::toResponse);
    }

    private Subject findSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    private void validateUniqueName(String name, Long subjectId) {
        if (name != null && !name.trim().isEmpty()) {
            boolean exists = subjectId == null
                    ? subjectRepository.existsByNameIgnoreCase(name.trim())
                    : subjectRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), subjectId);
            if (exists) {
                throw new IllegalArgumentException("Subject name already exists: " + name.trim());
            }
        }
    }

    private SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        SubjectResponse response = modelMapper.map(subject, SubjectResponse.class);
        if (subject.getStudents() != null) {
            response.setStudents(subject.getStudents().stream()
                    .map(this::toStudentResponse)
                    .toList());
        } else {
            response.setStudents(Collections.emptyList());
        }
        return response;
    }

    private StudentResponse toStudentResponse(Student student) {
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
