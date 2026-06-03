package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.vo.auth.LoginVO;
import com.xyehyin.hexuanning.vo.auth.RegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "token", source = "token")
    LoginVO toLoginVO(User user, String token);

    @Mapping(target = "token", source = "token")
    RegisterVO toRegisterVO(User user, String token);
}