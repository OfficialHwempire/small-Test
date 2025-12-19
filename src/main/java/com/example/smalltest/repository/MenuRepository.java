package com.example.smalltest.repository;

import com.example.smalltest.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu,Long> {

    //판매 가능한 메뉴 조회
    List<Menu> findByAvailableTrue();
}
