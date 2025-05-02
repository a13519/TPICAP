package net.zousys.mathtrading.interfaces.tpicap.repository;

import net.zousys.mathtrading.interfaces.tpicap.entity.TradeVaultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TradeVaultRepository extends JpaRepository<TradeVaultEntity, Integer> {
    @Query("SELECT t FROM TradeVaultEntity t WHERE FUNCTION('DATE', t.time) = :date")
    public List<TradeVaultEntity> findByTimeOnDate(@Param("date") LocalDate date);
    @Modifying
    @Query("DELETE FROM TradeVaultEntity t WHERE CAST(t.time AS DATE) != :date")
    void deleteAllExceptDate(@Param("date") LocalDate date);
}

