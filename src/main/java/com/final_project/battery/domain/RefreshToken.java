package com.final_project.battery.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "rt_key") // workerCode(사번)를 키로 사용
    private String key;

    @Column(name = "rt_value")
    private String value; // 실제 리프레시 토큰 값

    @Builder
    public RefreshToken(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void updateValue(String token) {
        this.value = token;
    }
}