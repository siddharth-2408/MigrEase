package com.migrease.repository;

import com.migrease.model.SupportRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRepository extends JpaRepository<SupportRequests, Long> {

    List<SupportRequests> findByEmail(String email);

    List<SupportRequests> findByStatus(SupportRequests.SupportStatus status);

    List<SupportRequests> findByCategory(SupportRequests.SupportCategory category);
}
