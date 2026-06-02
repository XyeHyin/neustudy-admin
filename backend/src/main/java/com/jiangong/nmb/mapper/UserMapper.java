package com.jiangong.nmb.mapper;

import com.jiangong.nmb.dto.user.CreateUserDTO;
import com.jiangong.nmb.dto.user.UpdateUserDTO;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.vo.user.UserDetailVO;
import com.jiangong.nmb.vo.user.UserSimpleVO;
import com.jiangong.nmb.vo.user.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserVO toUserVO(User user);

    UserDetailVO toUserDetailVO(User user);

    /**
     * User -> UserSimpleVO
     */
    UserSimpleVO toUserSimpleVO(User user);

    @Mapping(target = "enabled", constant = "true")
    User toUser(CreateUserDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateUserFromDto(UpdateUserDTO dto, @MappingTarget User user);
}