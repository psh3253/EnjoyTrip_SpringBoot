package com.gumi.enjoytrip.domain.user.dto;

import com.gumi.enjoytrip.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserUpdateDto {
    private long id;
    private String nickname;

    public User toEntity() {
        return User.builder()
                .nickname(nickname)
                .build();
    }
}
