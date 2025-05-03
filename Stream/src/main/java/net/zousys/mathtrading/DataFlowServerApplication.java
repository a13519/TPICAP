package net.zousys.mathtrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesAutoConfiguration;

@EnableDataFlowServer
@SpringBootApplication(
        exclude = {
//                SessionAutoConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class,
//                UserDetailsServiceAutoConfiguration.class,
//                LocalDeployerAutoConfiguration.class,
                CloudFoundryDeployerAutoConfiguration.class,
                KubernetesAutoConfiguration.class}
)
public class DataFlowServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataFlowServerApplication.class, args);
    }
}