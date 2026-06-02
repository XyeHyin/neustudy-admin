package com.jiangong.nmb.mapper;

import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.vo.auth.LoginVO;
import com.jiangong.nmb.vo.auth.RegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "token", source = "token")
    LoginVO toLoginVO(User user, String token);

    @Mapping(target = "token", source = "token")
    RegisterVO toRegisterVO(User user, String token);
}