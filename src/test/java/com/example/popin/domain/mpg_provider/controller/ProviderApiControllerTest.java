package com.example.popin.domain.mpg_provider.controller;

import com.example.popin.domain.mpg_provider.service.ProviderService;
import com.example.popin.domain.space.entity.Space;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 컨트롤러 슬라이스 테스트
 * - 보안필터 OFF
 * - Service만 Mock
 * - 엔티티는 Mockito.mock() 사용시 테스트 안됨 → 실제 인스턴스(서브클래스) 사용
 */
@WebMvcTest(controllers = ProviderApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProviderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderService providerService;

    /**
     * Space는 기본 생성자가 protected라서 외부 패키지에서 new Space() 불가.
     * → 테스트용 서브클래스로 super() 호출해 인스턴스화.
     */
    //space가 protected로 인스턴스 생성 불가
    static class TestSpace extends Space {
        public TestSpace() {
            super(); // protected 생성자 접근 가능 (서브클래스이므로)
        }
    }

    private Space createSpace(Long id, String title) {
        Space s = new TestSpace();

        //Reflection으로 필드 주입
        ReflectionTestUtils.setField(s, "id", id);
        ReflectionTestUtils.setField(s, "title", title);
        ReflectionTestUtils.setField(s, "isPublic", true);
        ReflectionTestUtils.setField(s, "startDate", LocalDate.of(2025, 9, 5));
        ReflectionTestUtils.setField(s, "endDate", LocalDate.of(2025, 9, 20));

        return s;
    }

    @Test
    void mySpaces_shouldReturn200_andList_whenPrincipalProvided() throws Exception {
        // given
        String email = "test@example.com";
        Space s1 = createSpace(1L, "Space-A");
        Space s2 = createSpace(2L, "Space-B");
        when(providerService.findMySpaces(eq(email))).thenReturn(List.of(s1, s2));

        // when & then
        mockMvc.perform(get("/api/provider/spaces").principal(() -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Space-A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Space-B"));
    }

    @Test
    void mySpaces_shouldReturnEmptyList_whenServiceReturnsEmpty() throws Exception {
        // given
        String email = "empty@example.com";
        when(providerService.findMySpaces(eq(email))).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/provider/spaces").principal(() -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
