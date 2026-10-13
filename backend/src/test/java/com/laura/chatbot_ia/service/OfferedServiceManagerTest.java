package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import com.laura.chatbot_ia.service.OfferedServiceManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Probamos el gestor de servicios aislándolo de la base de datos real.
@ExtendWith(MockitoExtension.class)
class OfferedServiceManagerTest {

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private OfferedServiceManager offeredServiceManager;

    // Verificamos que se consulta el repositorio de servicios activos.
    @Test
    void findByBusinessIdReturnsOnlyActiveServices() {
        Long businessId = 1L;
        OfferedService activeService = new OfferedService();

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByBusinessIdAndActiveTrue(businessId))
                .thenReturn(List.of(activeService));

        List<OfferedService> result = offeredServiceManager.findByBusinessId(businessId);

        assertEquals(1, result.size());
        assertSame(activeService, result.get(0));
        verify(offeredServiceRepository)
                .findByBusinessIdAndActiveTrue(businessId);
    }
}