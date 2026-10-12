package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.request.UserCreateRequest;
import com.example.qiyegongdansys.dto.request.UserUpdateRequest;
import com.example.qiyegongdansys.dto.response.UserResponse;
import com.example.qiyegongdansys.entity.Role;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.repository.RoleRepository;
import com.example.qiyegongdansys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRealName(req.realName());
        user.setPhone(req.phone());
        user.setEmail(req.email());
        user.setDepartment(req.department());
        user.setEnabled(true);
        user.setRoles(resolveRoles(req.roles()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        User user = find(id);
        if (StringUtils.hasText(req.password())) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        if (StringUtils.hasText(req.realName())) {
            user.setRealName(req.realName());
        }
        if (req.phone() != null) {
            user.setPhone(req.phone());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.department() != null) {
            user.setDepartment(req.department());
        }
        if (req.enabled() != null) {
            user.setEnabled(req.enabled());
        }
        if (req.roles() != null) {
            user.setRoles(resolveRoles(req.roles()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = find(id);
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("内置管理员不允许删除");
        }
        userRepository.delete(user);
    }

    public Page<UserResponse> list(String username, String realName, Integer page, Integer size) {
        PageRequest pageable = PageRequest.of(orDefault(page, 0), orDefault(size, 10),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<User> realSpec = (root, q, cb) -> {
            var predicate = cb.conjunction();
            if (StringUtils.hasText(username)) {
                predicate = cb.and(predicate, cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(realName)) {
                predicate = cb.and(predicate, cb.like(root.get("realName"), "%" + realName + "%"));
            }
            return predicate;
        };
        return userRepository.findAll(realSpec, pageable).map(UserResponse::from);
    }

    public Set<UserResponse> listByRole(String roleCode) {
        return userRepository.findUsersByRoleCode(roleCode).stream()
                .map(UserResponse::from)
                .collect(Collectors.toSet());
    }

    public User finEntity(Long id) {
        return find(id);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        Set<Role> roles = new HashSet<>();
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new BusinessException("至少需要分配一个角色");
        }
        for (String code : roleCodes) {
            roles.add(roleRepository.findByCode(code)
                    .orElseThrow(() -> new BusinessException("角色不存在: " + code)));
        }
        return roles;
    }

    private int orDefault(Integer v, int def) {
        return v == null ? def : v;
    }
}