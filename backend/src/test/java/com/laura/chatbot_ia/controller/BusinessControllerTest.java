package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Probamos las respuestas HTTP del controlador de negocios aislándolo de PostgreSQL.
@WebMvcTest(BusinessController.class)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusinessService businessService;

    private static final String AUTHENTICATED_EMAIL = "ana@clinica.es";

    // Comprobamos que el listado vacío devuelve HTTP 200 y un array JSON.
    @Test
    void findAllReturnsOkAndEmptyList() throws Exception {
        when(businessService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(businessService).findAll();
    }

    // Comprobamos que el listado devuelve los datos públicos del negocio.
    @Test
    void findAllReturnsOkAndBusinessData() throws Exception {
        Business business = business("Clínica Demo", "contacto@clinica-demo.es", "600123456");

        when(businessService.findAll()).thenReturn(List.of(business));

        mockMvc.perform(get("/api/businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Clínica Demo"))
                .andExpect(jsonPath("$[0].contactEmail").value("contacto@clinica-demo.es"))
                .andExpect(jsonPath("$[0].phoneNumber").value("600123456"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(businessService).findAll();
    }

    // Comprobamos que consultar un negocio existente devuelve HTTP 200.
    @Test
    void findByIdReturnsOkWhenBusinessExists() throws Exception {
        Business business = business("Clínica Demo", "contacto@clinica-demo.es", "600123456");

        when(businessService.findById(1L)).thenReturn(Optional.of(business));

        mockMvc.perform(get("/api/businesses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clínica Demo"))
                .andExpect(jsonPath("$.contactEmail").value("contacto@clinica-demo.es"))
                .andExpect(jsonPath("$.phoneNumber").value("600123456"))
                .andExpect(jsonPath("$.active").value(true));

        verify(businessService).findById(1L);
    }

    // Comprobamos que un negocio inexistente devuelve HTTP 404 sin cuerpo.
    @Test
    void findByIdReturnsNotFoundWhenBusinessDoesNotExist() throws Exception {
        when(businessService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/businesses/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(businessService).findById(99L);
    }

    // Comprobamos que crear un negocio válido devuelve HTTP 201.
    @Test
    void createReturnsCreatedWhenBusinessIsValid() throws Exception {
        Business savedBusiness = business(
                "Clínica Demo",
                "contacto@clinica-demo.es",
                "600123456");

        when(businessService.create(any(Business.class), eq(AUTHENTICATED_EMAIL)))
                .thenReturn(savedBusiness);

        mockMvc.perform(post("/api/businesses")
                .with(authenticatedUser(AUTHENTICATED_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo",
                            "contactEmail": "contacto@clinica-demo.es",
                            "phoneNumber": "600123456"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Clínica Demo"))
                .andExpect(jsonPath("$.contactEmail").value("contacto@clinica-demo.es"))
                .andExpect(jsonPath("$.phoneNumber").value("600123456"))
                .andExpect(jsonPath("$.active").value(true));

        verify(businessService).create(any(Business.class), eq(AUTHENTICATED_EMAIL));
    }

    // Comprobamos que el correo es opcional al crear un negocio.
    @Test
    void createReturnsCreatedWhenEmailIsOmitted() throws Exception {
        Business savedBusiness = new Business();
        savedBusiness.setName("Clínica Demo");

        when(businessService.create(any(Business.class), eq(AUTHENTICATED_EMAIL)))
                .thenReturn(savedBusiness);

        mockMvc.perform(post("/api/businesses")
                .with(authenticatedUser(AUTHENTICATED_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Clínica Demo"));

        verify(businessService).create(any(Business.class), eq(AUTHENTICATED_EMAIL));
    }

    // Comprobamos que un nombre de 150 caracteres, el máximo permitido, se acepta.
    @Test
    void createReturnsCreatedWhenNameIsAtMaxLength() throws Exception {
        String name = "a".repeat(150);
        Business savedBusiness = new Business();
        savedBusiness.setName(name);

        when(businessService.create(any(Business.class), eq(AUTHENTICATED_EMAIL)))
                .thenReturn(savedBusiness);

        mockMvc.perform(post("/api/businesses")
                .with(authenticatedUser(AUTHENTICATED_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));

        verify(businessService).create(any(Business.class), eq(AUTHENTICATED_EMAIL));
    }

    // Comprobamos que un nombre en blanco devuelve HTTP 400.
    @Test
    void createReturnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": " ",
                            "contactEmail": "contacto@clinica-demo.es",
                            "phoneNumber": "600123456"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("must not be blank"))
                .andExpect(jsonPath("$.path").value("/api/businesses"));

        verifyNoInteractions(businessService);
    }

    // Comprobamos que un nombre por encima de 150 caracteres devuelve HTTP 400.
    @Test
    void createReturnsBadRequestWhenNameExceedsMaxLength() throws Exception {
        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "%s",
                            "contactEmail": "contacto@clinica-demo.es"
                        }
                        """.formatted("a".repeat(151))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El nombre no puede superar los 150 caracteres"));

        verifyNoInteractions(businessService);
    }

    // Comprobamos que un correo con formato incorrecto devuelve HTTP 400.
    @Test
    void createReturnsBadRequestWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo",
                            "contactEmail": "correo-no-valido",
                            "phoneNumber": "600123456"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El correo electrónico no tiene un formato válido"))
                .andExpect(jsonPath("$.path").value("/api/businesses"));

        verifyNoInteractions(businessService);
    }

    // Comprobamos que un email autenticado sin cuenta devuelve HTTP 401.
    @Test
    void createReturnsUnauthorizedWhenAuthenticatedUserDoesNotExist() throws Exception {
        when(businessService.create(any(Business.class), eq(AUTHENTICATED_EMAIL)))
                .thenThrow(new AuthenticatedUserNotFoundException());

        mockMvc.perform(post("/api/businesses")
                .with(authenticatedUser(AUTHENTICATED_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("No existe una cuenta para el usuario autenticado"))
                .andExpect(jsonPath("$.path").value("/api/businesses"));

        verify(businessService).create(any(Business.class), eq(AUTHENTICATED_EMAIL));
    }

    // Comprobamos que una petición sin identidad no crea el negocio.
    @Test
    void createReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("No existe una cuenta para el usuario autenticado"))
                .andExpect(jsonPath("$.path").value("/api/businesses"));

        verifyNoInteractions(businessService);
    }

    // Comprobamos que la autenticación anónima de Spring Security no actúa como autora.
    @Test
    void createReturnsUnauthorizedWhenAuthenticationIsAnonymous() throws Exception {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        mockMvc.perform(post("/api/businesses")
                .with(request -> {
                    request.setUserPrincipal(anonymous);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Clínica Demo"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("No existe una cuenta para el usuario autenticado"))
                .andExpect(jsonPath("$.path").value("/api/businesses"));

        verifyNoInteractions(businessService);
    }

    // Deja en la petición el mismo principal que el filtro JWT guarda al validar el token.
    private RequestPostProcessor authenticatedUser(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        return request -> {
            request.setUserPrincipal(authentication);
            return request;
        };
    }

    private Business business(String name, String contactEmail, String phoneNumber) {
        Business business = new Business();
        business.setName(name);
        business.setContactEmail(contactEmail);
        business.setPhoneNumber(phoneNumber);
        business.setActive(true);
        return business;
    }
}
