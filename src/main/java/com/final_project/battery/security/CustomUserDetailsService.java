package com.final_project.battery.security;

import com.final_project.battery.domain.Worker;
import com.final_project.battery.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final WorkerRepository workerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String workerCode) throws UsernameNotFoundException {
        return workerRepository.findByWorkerCode(workerCode)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(workerCode + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // DB 에 있는 Worker 정보를 UserDetails 객체로 변환
    private UserDetails createUserDetails(Worker worker) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(worker.getRole().toString());

        return new User(
                String.valueOf(worker.getWorkerId()), // Principal에 ID(PK)를 넣거나, workerCode를 넣을 수 있음
                worker.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}