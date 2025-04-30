package net.zousys.mathtrading.interfaces.tpicap.model;

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
    private String proxyHost;
    private int proxyPort;
}
