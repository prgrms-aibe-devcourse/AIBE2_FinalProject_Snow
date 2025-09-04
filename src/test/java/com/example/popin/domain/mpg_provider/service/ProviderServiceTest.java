package com.example.popin.domain.mpg_provider.service;

import com.example.popin.domain.space.entity.Space;
import com.example.popin.domain.space.repository.SpaceRepository;
import com.example.popin.domain.user.User;
import com.example.popin.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    SpaceRepository spaceRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ProviderService providerService;

    @Test
    @DisplayName("이메일로 사용자 찾고, 해당 소유자의 공간 목록을 반환한다")
    void findMySpaces_returnsList_whenUserExists() {
        // given
        String email = "owner@example.com";
        User owner = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(owner);

        // Space는 컨트롤러처럼 직렬화하지 않으니 mock으로 충분
        Space s1 = mock(Space.class);
        Space s2 = mock(Space.class);
        when(s1.getTitle()).thenReturn("첫 번째 공간 제목");
        when(s2.getTitle()).thenReturn("두 번째 공간 제목");
        when(spaceRepository.findByOwner(eq(owner))).thenReturn(List.of(s1, s2));

        // when
        List<Space> result = providerService.findMySpaces(email);

        // then
        assertThat(result).hasSize(2).containsExactly(s1, s2);
        verify(userRepository, times(1)).findByEmail(email);
        verify(spaceRepository, times(1)).findByOwner(owner);
        verifyNoMoreInteractions(userRepository, spaceRepository);

        assertThat(result.get(0).getTitle()).isEqualTo("첫 번째 공간 제목");
        assertThat(result.get(1).getTitle()).isEqualTo("두 번째 공간 제목");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 없으면 IllegalArgumentException을 던진다")
    void findMySpaces_throws_whenUserNotFound() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> providerService.findMySpaces(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 없음");

        verify(userRepository, times(1)).findByEmail(email);
        verify(spaceRepository, never()).findByOwner(any());
    }

    @Test
    @DisplayName("사용자가 존재하지만 공간이 없으면 빈 리스트를 반환한다")
    void findMySpaces_returnsEmpty_whenNoSpaces() {
        // given
        String email = "owner@example.com";
        User owner = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(owner);
        when(spaceRepository.findByOwner(owner)).thenReturn(List.of());

        // when
        List<Space> result = providerService.findMySpaces(email);

        // then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
        verify(spaceRepository).findByOwner(owner);
    }
}
