package net.zousys.mathtrading.interfaces.icap;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServerSignature {
    private String host;
    private int port;
    private String key;
    private String value;
    private boolean ssl;
}
