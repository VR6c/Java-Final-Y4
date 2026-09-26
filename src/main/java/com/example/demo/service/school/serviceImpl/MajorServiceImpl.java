package com.example.demo.service.school.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.MajorPatchRequest;
import com.example.demo.dto.request.school.MajorRequest;
import com.example.demo.dto.response.school.MajorResponse;
import com.example.demo.dto.response.school.StudentResponse;
import com.example.demo.entity.school.Card;
import com.example.demo.entity.school.Major;
import com.example.demo.entity.school.Student;
import com.example.demo.entity.school.Subject;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.school.MajorRepository;
import com.example.demo.service.school.MajorService;
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
public class MajorServiceImpl implements MajorService {

    private final MajorRepository majorRepository;
    private final ModelMapper modelMapper;

    public MajorServiceImpl(MajorRepository majorRepository, ModelMapper modelMapper) {
        this.majorRepository = majorRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<MajorResponse> getAll(Pageable pageable) {
        return majorRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_MAJORS, key = "#id")
    public MajorResponse getById(Long id) {
        return toResponse(findMajorById(id));
    }

    @Override
    public MajorResponse create(MajorRequest request) {
        String trimmedName = request.name() != null ? request.name().trim() : null;
        validateUniqueName(trimmedName, null);

        Major major = new Major();
        major.setName(trimmedName);
        major.setDescription(request.description() != null ? request.description().trim() : null);

        return toResponse(majorRepository.save(major));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, key = "#id")
    public MajorResponse update(Long id, MajorRequest input) {
        Major existingMajor = findMajorById(id);
        String trimmedName = input.name() != null ? input.name().trim() : null;
        validateUniqueName(trimmedName, id);

        existingMajor.setName(trimmedName);
        existingMajor.setDescription(input.description() != null ? input.description().trim() : null);

        return toResponse(majorRepository.save(existingMajor));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, key = "#id")
    public MajorResponse edit(Long id, MajorPatchRequest input) {
        Major existingMajor = findMajorById(id);

        if (input.name() != null) {
            String trimmedName = input.name().trim();
            validateUniqueName(trimmedName, id);
            existingMajor.setName(trimmedName);
        }
        if (input.description() != null) {
            existingMajor.setDescription(input.description().trim());
        }

        return toResponse(majorRepository.save(existingMajor));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, key = "#id")
    public void deleteById(Long id) {
        majorRepository.delete(findMajorById(id));
    }

    @Override
    public Page<MajorResponse> search(String name, String description, Pageable pageable) {
        return majorRepository.searchMajors(name, description, pageable)
                .map(this::toResponse);
    }

    private Major findMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with id: " + id));
    }

    private void validateUniqueName(String name, Long majorId) {
        if (name != null && !name.trim().isEmpty()) {
            boolean exists = majorId == null
                    ? majorRepository.existsByNameIgnoreCase(name.trim())
                    : majorRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), majorId);
            if (exists) {
                throw new IllegalArgumentException("Major name already exists: " + name.trim());
            }
        }
    }

    private MajorResponse toResponse(Major major) {
        if (major == null) {
            return null;
        }
        MajorResponse response = modelMapper.map(major, MajorResponse.class);
        if (major.getStudents() != null) {
            response.setStudents(major.getStudents().stream()
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
