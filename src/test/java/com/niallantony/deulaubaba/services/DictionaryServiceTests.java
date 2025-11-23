package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryEntryDTO;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryListingsResponse;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPostRequest;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPutRequest;
import com.niallantony.deulaubaba.exceptions.InvalidDictionaryDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.DictionaryMapper;
import com.niallantony.deulaubaba.util.DictionaryTestFactory;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DictionaryServiceTests {
    @Mock
    private DictionaryRepository dictionaryRepository;
    @Mock
    private CommunicationCategoryRepository categoryRepository;
    @Mock
    private JsonUtils jsonUtils;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private StudentService studentService;
    @Mock
    private DictionaryMapper dictionaryMapper;
    @InjectMocks
    private DictionaryService dictionaryService;
    Student mockStudent = new Student();
    Set<CommunicationCategory> mockCategories = new HashSet<>();
    DictionaryEntry mockEntry = new DictionaryEntry(
            123L,
            mockStudent,
            ExpressionType.BODY,
            "Title",
            mockCategories,
            "./example.png",
            "Description"
    );

    private void commonPostStubs(Student student) {
            when(studentService.getAuthorisedStudent("abc", "userId"))
                    .thenReturn(student);
            when(categoryRepository.findByLabel(CommunicationCategoryLabel.PAIN))
                    .thenReturn(Optional.of(new CommunicationCategory()));
            when(dictionaryRepository.save(any())).thenReturn(new DictionaryEntry());
    }

    private void commonPutStubs() {
        when(studentService.studentBelongsToUser("abc", "userId"))
                .thenReturn(true);
        when(categoryRepository.findByLabel(CommunicationCategoryLabel.PAIN))
                .thenReturn(Optional.of(new CommunicationCategory()));
        when(categoryRepository.findByLabel(CommunicationCategoryLabel.SHOWME))
                .thenReturn(Optional.of(new CommunicationCategory()));
        when(dictionaryRepository.findById(1L)).thenReturn(Optional.of(mockEntry));
    }

    @Test
    public void getDictionaryListings_whenGivenValidInputsAndListingsExist_returnsDictionaryListingsResponse() {
        List<DictionaryEntry> mockListings = new ArrayList<>();
        DictionaryEntryDTO mockEntryDTO = new DictionaryEntryDTO();
        mockListings.add(mockEntry);

        when(studentService.getAuthorisedStudent("abc", "123")).thenReturn(mockStudent);
        when(dictionaryRepository.findAllByStudent(mockStudent)).thenReturn(mockListings);
        when(dictionaryMapper.entityToDto(mockEntry)).thenReturn(mockEntryDTO);

        DictionaryListingsResponse response = dictionaryService.getDictionaryListings("abc", "123");
        verify(dictionaryRepository, times(1)).findAllByStudent(mockStudent);
        verify(studentService, times(1)).getAuthorisedStudent("abc", "123");
        verify(dictionaryMapper, times(1)).entityToDto(mockEntry);

        assertNotNull(response);
        assertTrue(response.getExpressiontypes().contains(ExpressionType.BODY));
        assertEquals(mockEntryDTO, response.getListings().getFirst());
    }

    @Test
    public void getDictionaryListings_whenGivenValidInputsButNoListingsExist_returnsEmptyDictionaryListingsResponse() {
        when(studentService.getAuthorisedStudent("abc", "123")).thenReturn(mockStudent);
        when(dictionaryRepository.findAllByStudent(mockStudent)).thenReturn(new ArrayList<>());

        DictionaryListingsResponse response = dictionaryService.getDictionaryListings("abc", "123");
        verify(studentService, times(1)).getAuthorisedStudent("abc", "123");
        verify(dictionaryRepository, times(1)).findAllByStudent(mockStudent);

        assertInstanceOf(DictionaryListingsResponse.class, response);
        assertNull(response.getExpressiontypes());
        assertNull(response.getListings());
    }

    @Test
    public void getDictionaryListings_whenGivenInvalidStudentId_throwsError() {
        ResourceNotFoundException noStudent = new ResourceNotFoundException("Student not found");
        when(studentService.getAuthorisedStudent("abc", "123")).thenThrow(noStudent);
        RuntimeException ex = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.getDictionaryListings("abc", "123"));
        assertEquals("Student not found", ex.getMessage());
    }

    @Test
    public void getDictionaryListings_whenGivenInvalidUserId_throwsError() {
        UserNotAuthorizedException notUser = new UserNotAuthorizedException("User not found");
        when(studentService.getAuthorisedStudent("abc", "123")).thenThrow(notUser);
        RuntimeException ex = assertThrows(UserNotAuthorizedException.class, () -> dictionaryService.getDictionaryListings("abc", "123"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void addDictionaryEntry_whenNoImage_savesEntry()  {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();

        Student student = new Student();
        commonPostStubs(student);


        DictionaryEntry result = dictionaryService.addDictionaryEntry(request, null, "userId");
        assertEquals(student, result.getStudent());
        verify(dictionaryRepository).save(result);
    }
    @Test
    void addDictionaryEntry_whenImage_savesEntry() {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        MultipartFile image = mock(MultipartFile.class);
        Student student = new Student();
        commonPostStubs(student);


        DictionaryEntry result = dictionaryService.addDictionaryEntry(request, image, "userId");
        verify(fileStorageService).swapImage(eq(image), any());

        assertEquals(student, result.getStudent());
        verify(dictionaryRepository).save(result);
    }

    @Test
    void addDictionaryEntry_whenImageFails_stillSavesEntry() {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        String json = "json-placeholder";
        MultipartFile image = mock(MultipartFile.class);
        Student student = new Student();
        commonPostStubs(student);
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());


        DictionaryEntry result = dictionaryService.addDictionaryEntry(request, image, "userId");
        verify(fileStorageService).swapImage(eq(image), any());

        assertEquals(student, result.getStudent());
        verify(dictionaryRepository).save(result);
    }

    @Test
    void updateDictionaryEntry_whenGivenValidDataWithoutImage_savesEntry() {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        commonPutStubs();

        dictionaryService.updateDictionaryEntry(request, null, "userId");
        verify(dictionaryRepository).save(argThat(arg ->
                arg.getDescription().equals(request.getDescription()) &&
                        arg.getTitle().equals(request.getTitle())
        ));
    }

    @Test
    void updateDictionaryEntry_whenGivenValidDataWithImage_savesEntry() {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        MultipartFile image = mock(MultipartFile.class);
        commonPutStubs();
        dictionaryService.updateDictionaryEntry(request, image, "userId");
        verify(dictionaryRepository).save(argThat(arg ->
                arg.getDescription().equals(request.getDescription()) &&
                        arg.getTitle().equals(request.getTitle())
        ));
        verify(fileStorageService, times(1)).swapImage(eq(image), any());
    }

    @Test
    void updateDictionaryEntry_whenGivenInvalidData_throwsError() {
        DictionaryPutRequest request = new DictionaryPutRequest();
        assertThrows(InvalidDictionaryDataException.class, () -> dictionaryService.updateDictionaryEntry(request, null, "userId"));
    }

    @Test
    void updateDictionaryEntry_whenEntryDoesntExist_throwsError() {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        when(studentService.studentBelongsToUser("abc", "userId"))
                .thenReturn(true);
        when(dictionaryRepository.findById(1L)).thenThrow(ResourceNotFoundException.class);
        assertThrows(ResourceNotFoundException.class, () -> dictionaryService.updateDictionaryEntry(request, null, "userId"));
    }

    @Test
    void updateDictionaryEntry_whenUserNotAuthorized_throwsError() {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        when(studentService.studentBelongsToUser("abc", "userId"))
                .thenReturn(false);
        assertThrows(UserNotAuthorizedException.class, () -> dictionaryService.updateDictionaryEntry(request, null, "userId"));
    }

    @Test
    void deleteDictionaryEntry_whenGivenValidData_deletesEntry() {
        DictionaryEntry entry = new DictionaryEntry();
        Student student = new Student();
        student.setStudentId("123");
        entry.setStudent(student);
        entry.setImgsrc("./example.png");
        when(dictionaryRepository.existsById(123L)).thenReturn(true);
        when(dictionaryRepository.getReferenceById(123L)).thenReturn(entry);
        when(studentService.studentBelongsToUser("123", "userId")).thenReturn(true);
        dictionaryService.deleteDictionaryEntry(123L, "userId");
        verify(dictionaryRepository).deleteById(123L);
        verify(fileStorageService).deleteImage(eq("./example.png"),any());
    }


    @Test
    void deleteDictionaryEntry_whenEntryDoesntExist_throwsError() {
        DictionaryEntry entry = new DictionaryEntry();
        Student student = new Student();
        student.setStudentId("123");
        entry.setStudent(student);
        when(dictionaryRepository.existsById(123L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> dictionaryService.deleteDictionaryEntry(123L, "userId"));
    }

    @Test
    void deleteDictionaryEntry_whenUserNotAuthorized_throwsError() {
        DictionaryEntry entry = new DictionaryEntry();
        Student student = new Student();
        student.setStudentId("123");
        entry.setStudent(student);
        when(dictionaryRepository.existsById(123L)).thenReturn(true);
        when(dictionaryRepository.getReferenceById(123L)).thenReturn(entry);
        when(studentService.studentBelongsToUser("123", "userId")).thenReturn(false);
        assertThrows(UserNotAuthorizedException.class, () -> dictionaryService.deleteDictionaryEntry(123L, "userId"));
    }
}
