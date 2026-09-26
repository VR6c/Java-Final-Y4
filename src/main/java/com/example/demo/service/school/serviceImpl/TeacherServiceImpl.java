package com.example.demo.service.school.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.dto.patch.school.TeacherPatchRequest;
import com.example.demo.dto.request.school.TeacherRequest;
import com.example.demo.dto.response.school.TeacherResponse;
import com.example.demo.entity.school.Teacher;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.school.TeacherRepository;
import com.example.demo.service.school.TeacherService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;

    public TeacherServiceImpl(TeacherRepository teacherRepository, ModelMapper modelMapper) {
        this.teacherRepository = teacherRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<TeacherResponse> getAll(Pageable pageable) {
        return teacherRepository.findAll(pageable)
                .map(t -> modelMapper.map(t, TeacherResponse.class));
    }

    @Override
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = modelMapper.map(request, Teacher.class);
        return modelMapper.map(teacherRepository.save(teacher), TeacherResponse.class);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_TEACHERS, key = "#id")
    public TeacherResponse getById(Long id) {
        return modelMapper.map(findTeacherById(id), TeacherResponse.class);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_TEACHERS, key = "#id")
    public TeacherResponse update(Long id, TeacherRequest input) {
        Teacher existingTeacher = findTeacherById(id);
        modelMapper.map(input, existingTeacher);
        return modelMapper.map(teacherRepository.save(existingTeacher), TeacherResponse.class);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_TEACHERS, key = "#id")
    public TeacherResponse edit(Long id, TeacherPatchRequest input) {
        Teacher existingTeacher = findTeacherById(id);
        if (input.name() != null) {
            existingTeacher.setName(input.name());
        }
        if (input.subject() != null) {
            existingTeacher.setSubject(input.subject());
        }
        return modelMapper.map(teacherRepository.save(existingTeacher), TeacherResponse.class);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_TEACHERS, key = "#id")
    public void deleteById(Long id) {
        teacherRepository.delete(findTeacherById(id));
    }

    @Override
    public Page<TeacherResponse> search(String name, String subject, Pageable pageable) {
        return teacherRepository.searchTeachers(name, subject, pageable)
                .map(t -> modelMapper.map(t, TeacherResponse.class));
    }

    private Teacher findTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
    }
}
