package com.example.qiyegongdansys.config;

import com.example.qiyegongdansys.entity.Role;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.entity.WorkOrderType;
import com.example.qiyegongdansys.repository.RoleRepository;
import com.example.qiyegongdansys.repository.UserRepository;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import com.example.qiyegongdansys.repository.WorkOrderTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 系统初始化种子数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final WorkOrderTypeRepository typeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            return;
        }
        seedRoles();
        seedTypes();
        seedUsers();
        log.info("初始化种子数据完成。默认账号: admin / admin123, agent1-3 / agent123");
    }

    private void seedRoles() {
        createRoleIfAbsent("ADMIN", "系统管理员");
        createRoleIfAbsent("MANAGER", "业务主管");
        createRoleIfAbsent("AGENT", "处理人员");
        createRoleIfAbsent("CUSTOMER", "客户/普通用户");
    }

    private void createRoleIfAbsent(String code, String name) {
        if (roleRepository.findByCode(code).isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            roleRepository.save(role);
        }
    }

    private void seedTypes() {
        createType("HARDWARE", "硬件故障", "电脑、打印机等硬件设备故障",
                "[{\"field\":\"assetCode\",\"label\":\"固定资产编号\",\"type\":\"text\",\"required\":false}]");
        createType("SOFTWARE", "软件异常", "操作系统或业务软件异常",
                "[{\"field\":\"software\",\"label\":\"软件名称\",\"type\":\"text\",\"required\":true},{\"field\":\"version\",\"label\":\"版本号\",\"type\":\"text\",\"required\":false}]");
        createType("NETWORK", "网络问题", "网络连接、带宽等问题",
                "[{\"field\":\"location\",\"label\":\"故障位置\",\"type\":\"text\",\"required\":false}]");
        createType("SERVICE", "服务请求", "通用服务申请与协助",
                "[{\"field\":\"department\",\"label\":\"申请部门\",\"type\":\"text\",\"required\":false}]");
    }

    private void createType(String code, String name, String desc, String customFields) {
        if (typeRepository.findByCode(code).isEmpty()) {
            WorkOrderType t = new WorkOrderType();
            t.setCode(code);
            t.setName(name);
            t.setDescription(desc);
            t.setCustomFields(customFields);
            t.setBuiltin(true);
            t.setEnabled(true);
            typeRepository.save(t);
        }
    }

    private void seedUsers() {
        createUser("admin", "admin123", "系统管理员", "ADMIN");
        createUser("manager", "manager123", "业务经理", "MANAGER");
        createUser("agent1", "agent123", "王技术员", "AGENT");
        createUser("agent2", "agent123", "李技术员", "AGENT");
        createUser("agent3", "agent123", "张技术员", "AGENT");
        createUser("customer", "customer123", "刘用户", "CUSTOMER");
    }

    private void createUser(String username, String pwd, String realName, String roleCode) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(pwd));
        user.setRealName(realName);
        user.setEmail(username + "@example.com");
        user.setEnabled(true);
        Role role = roleRepository.findByCode(roleCode).orElseThrow();
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
}