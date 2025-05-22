package com.migrease.service;

import com.migrease.model.SupportRequests;
import com.migrease.repository.SupportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportService {

    @Autowired
    private SupportRepository supportRepository;

    public SupportRequests saveSupportRequest(SupportRequests supportRequest) {
        // The @PrePersist in your entity will set createdAt and status
        return supportRepository.save(supportRequest);
    }

    // You can add more methods as needed for handling support requests
    // For example:

    public SupportRequests getRequestById(Long id) {
        return supportRepository.findById(id).orElse(null);
    }

    public void updateSupportRequestStatus(Long id, SupportRequests.SupportStatus status) {
        SupportRequests request = getRequestById(id);
        if (request != null) {
            request.setStatus(status);
            supportRepository.save(request);
        }
    }
}