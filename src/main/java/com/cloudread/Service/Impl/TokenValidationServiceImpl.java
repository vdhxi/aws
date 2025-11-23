package com.cloudread.Service.Impl;

import com.cloudread.Service.TokenValidationService;
import com.cloudread.Entity.TokenValid;
import com.cloudread.Repository.TokenValidRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenValidationServiceImpl implements TokenValidationService {
    TokenValidRepository tokenValidRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean validateTokenUsage(String token) {
        TokenValid tokenValid = tokenValidRepository.findByToken(token);
        if (tokenValid != null) {
            if (tokenValid.isExpired()) {
                return false;
            } else if (tokenValid.getCurrentUse() >= tokenValid.getAllowToUse()) {
                tokenValid.setExpired(true);
                tokenValidRepository.save(tokenValid);
                return false;
            } else {
                tokenValid.setCurrentUse(tokenValid.getCurrentUse() + 1);
                tokenValidRepository.save(tokenValid);
                return true;
            }
        } else {
            return false;
        }
    }
}
