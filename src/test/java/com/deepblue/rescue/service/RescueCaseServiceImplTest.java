package com.deepblue.rescue.service;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {
     @Mock 
     private RescueCaseRepository repository;

     @Mock 
     private RescueCaseMapper mapper;

     @InjectMocks 
     private RescueCaseServiceImpl service;

     @Test 
     void shouldFindRescueCaseByCode(){

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-001");

        RescueCaseResponse response = new RescueCaseResponse(
            1L,
            "RES-001",
            null,
            null,
            null,
            null,
            null

        );
        when(
            repository.findByCaseCode("RES-001")
        ).thenReturn(
            Optional.of(rescueCase)
        );

        when(
            mapper.toResponse(rescueCase)
        ).thenReturn(response);
        
        RescueCaseResponse result=
            service.findByCode("RES-001");

        assertThat(result)
            .isEqualTo(response);

        verify(repository)
            .findByCaseCode("RES-001");

        verify(mapper)
            .toResponse(rescueCase);        
     }

     @Test 
     void shouldThrowExceptionWhenRescueCaseNotFound(){
        when(
            repository.findByCaseCode("RES-999")
        ).thenReturn(
            Optional.empty()
        );

        assertThatThrownBy(() -> service.findByCode("RES-999"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Rescue case not found: RES-999");


        verify(repository)
            .findByCaseCode("RES-999");
        
            verify(mapper, never())
            .toResponse(any());
     }

     @Test 
     void shouldChangeStatusSuccessfully(){
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-001");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        ChangeRescueStatusRequest request =
            new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION);

        RescueCaseResponse response = new RescueCaseResponse(
            1L,
            "RES-001",
            null,
            null,
            RescueStatus.UNDER_EVALUATION,
            null,
            null
        );
                
        
        when(
            repository.findByCaseCode("RES-001")
        ).thenReturn( 
            Optional.of(rescueCase)
        );

        when(
            repository.save(rescueCase)
        ).thenReturn(rescueCase);

        when(
            mapper.toResponse(rescueCase)
        ).thenReturn(response);

        RescueCaseResponse result =
            service.changeStatus("RES-001", request);

        assertThat(result)
            .isEqualTo(response);
            
        verify(repository)
            .findByCaseCode("RES-001");
            
        verify(repository)
            .save(rescueCase);
            
        verify(mapper)
            .toResponse(rescueCase);

     }

     @Test 
     void shouldThrowExceptionWhenStatusTransitionIsInvalid(){
        String caseCode = "RES-001";
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode(caseCode);
        rescueCase.setStatus(RescueStatus.ADMITTED);

        ChangeRescueStatusRequest request =
            new ChangeRescueStatusRequest(RescueStatus.READY_FOR_RELEASE);

        when(
            repository.findByCaseCode(caseCode)
        ).thenReturn(
            Optional.of(rescueCase)
        );
        
        assertThatThrownBy(() -> service.changeStatus(caseCode, request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Invalid status transition from ADMITTED to READY_FOR_RELEASE");
        
        verify(repository)
            .findByCaseCode(caseCode);
            
        verify(repository, never())
            .save(any());
            
        verify(mapper, never())
            .toResponse(any());    

     }

     
    
}
