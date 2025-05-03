package net.zousys.mathtrading.interfaces.tpicap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "trade_vault",
        indexes = {@Index(name = "TV_ind_tradeid", columnList = "tradeId", unique = true),
                @Index(name = "TV_ind_time", columnList = "time", unique = false),
        })
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeVaultEntity {
    @Id
    private String tradeId;          // ticker
    private ZonedDateTime time;
    private int status;
}