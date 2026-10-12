package com.example.qiyegongdansys.repository;

import com.example.qiyegongdansys.entity.Role;
import com.example.qiyegongdansys.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("select r from Role r join r.users u where u.id = :userId")
    java.util.List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Query("select u from User u join u.roles r where r.code = :roleCode and u.enabled = true")
    java.util.List<User> findUsersByRoleCode(@Param("roleCode") String roleCode);
}