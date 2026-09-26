package com.example.demo.service.school.serviceImpl;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.CardPatchRequest;
import com.example.demo.dto.request.school.CardRequest;
import com.example.demo.dto.response.school.CardResponse;
import com.example.demo.entity.school.Card;
import com.example.demo.entity.school.Student;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.school.CardRepository;
import com.example.demo.repository.school.StudentRepository;
import com.example.demo.service.school.CardService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    public CardServiceImpl(CardRepository cardRepository,
                           StudentRepository studentRepository,
                           ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<CardResponse> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public CardResponse getById(Long id) {
        return toResponse(findCardById(id));
    }

    @Override
    public CardResponse create(CardRequest request) {
        Student student = null;
        if (request.studentId() != null) {
            student = findStudentById(request.studentId());
        }

        String effectiveCardNumber = resolveCardNumber(request.cardNumber(), student);
        validateUniqueConstraints(effectiveCardNumber, request.studentId(), null);

        Card card = new Card();
        card.setCardNumber(effectiveCardNumber);
        card.setIssueDate(request.issueDate());
        card.setStudent(student);

        return toResponse(cardRepository.save(card));
    }

    @Override
    public CardResponse update(Long id, CardRequest request) {
        Card existingCard = findCardById(id);

        Student student = null;
        if (request.studentId() != null) {
            student = findStudentById(request.studentId());
        }

        String effectiveCardNumber = resolveCardNumber(request.cardNumber(), student);
        validateUniqueConstraints(effectiveCardNumber, request.studentId(), id);

        existingCard.setCardNumber(effectiveCardNumber);
        existingCard.setIssueDate(request.issueDate());
        existingCard.setStudent(student);

        return toResponse(cardRepository.save(existingCard));
    }

    @Override
    public CardResponse edit(Long id, CardPatchRequest request) {
        Card existingCard = findCardById(id);

        boolean shouldUnlink = Boolean.TRUE.equals(request.unlinkStudent());
        Student student;
        if (shouldUnlink) {
            student = null;
        } else if (request.studentId() != null) {
            student = findStudentById(request.studentId());
        } else {
            student = existingCard.getStudent();
        }

        Long effectiveStudentId = student != null ? student.getId() : null;

        String effectiveCardNumber;
        if (request.cardNumber() != null && !request.cardNumber().trim().isEmpty()) {
            effectiveCardNumber = request.cardNumber().trim();
        } else if (student != null) {
            effectiveCardNumber = resolveCardNumber(null, student);
        } else {
            effectiveCardNumber = existingCard.getCardNumber();
        }

        validateUniqueConstraints(effectiveCardNumber, effectiveStudentId, id);

        existingCard.setCardNumber(effectiveCardNumber);
        if (request.issueDate() != null) {
            existingCard.setIssueDate(request.issueDate());
        }
        existingCard.setStudent(student);

        return toResponse(cardRepository.save(existingCard));
    }

    @Override
    public void deleteById(Long id) {
        cardRepository.delete(findCardById(id));
    }

    @Override
    public Page<CardResponse> search(String cardNumber, String studentName, Pageable pageable) {
        return cardRepository.searchCards(cardNumber, studentName, pageable)
                .map(this::toResponse);
    }

    private Card findCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private String resolveCardNumber(String rawCardNumber, Student student) {
        if (rawCardNumber != null && !rawCardNumber.trim().isEmpty()) {
            return rawCardNumber.trim();
        }
        if (student != null) {
            String majorName = (student.getMajor() != null) ? student.getMajor().getName() : null;
            return AppConstants.formatCardNumber(majorName, student.getId());
        }
        throw new IllegalArgumentException("Card number is required when student is not specified.");
    }

    private void validateUniqueConstraints(String cardNumber, Long studentId, Long cardId) {
        if (cardNumber != null && !cardNumber.trim().isEmpty()) {
            boolean cardExists = cardId == null
                    ? cardRepository.existsByCardNumber(cardNumber)
                    : cardRepository.existsByCardNumberAndIdNot(cardNumber, cardId);
            if (cardExists) {
                throw new IllegalArgumentException("Card number already exists: " + cardNumber);
            }
        }

        if (studentId != null) {
            boolean studentHasCard = cardId == null
                    ? cardRepository.existsByStudentId(studentId)
                    : cardRepository.existsByStudentIdAndIdNot(studentId, cardId);
            if (studentHasCard) {
                throw new IllegalArgumentException(
                        "Student with id " + studentId + " is already assigned to another card.");
            }
        }
    }

    public CardResponse toResponse(Card card) {
        if (card == null) {
            return null;
        }
        CardResponse response = modelMapper.map(card, CardResponse.class);

        if (card.getStudent() != null) {
            Student student = card.getStudent();
            response.setStudentId(student.getId());
            response.setStudentName(student.getName());
            if (student.getMajor() != null && student.getMajor().getName() != null && !student.getMajor().getName().trim().isEmpty()) {
                response.setCardNumber(AppConstants.formatCardNumber(student.getMajor().getName(), student.getId()));
            } else if (card.getCardNumber() != null && !card.getCardNumber().trim().isEmpty()) {
                response.setCardNumber(card.getCardNumber().trim());
            } else {
                response.setCardNumber(AppConstants.formatDefaultCardNumber(student.getId()));
            }
        } else {
            response.setCardNumber(card.getCardNumber());
        }

        return response;
    }
}
