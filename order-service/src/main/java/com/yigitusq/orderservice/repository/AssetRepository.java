package com.yigitusq.orderservice.repository;

import com.yigitusq.orderservice.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Kullanıcının belirli bir semboldeki varlığını bulmak için
    Optional<Asset> findByUserIdAndSymbol(Long userId, String symbol);
}