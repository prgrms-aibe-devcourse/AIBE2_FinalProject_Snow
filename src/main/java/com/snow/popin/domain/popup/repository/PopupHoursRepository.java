package com.snow.popin.domain.popup.repository;

import com.snow.popin.domain.popup.entity.PopupHours;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PopupHoursRepository extends JpaRepository<PopupHours, Long> {
    List<PopupHours> findByPopupId(Long popupId);
    void deleteByPopupId(Long popupId);
}