package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.UserStateEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShortUserInfo {
    String name;
    boolean enabled;

    public static ShortUserInfo map(UserStateEntity userStateEntity) {
        return new ShortUserInfo(userStateEntity.getName(), userStateEntity.isEnabled());
    }
}
