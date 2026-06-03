package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.user.CreateUserDTO;
import com.xyehyin.hexuanning.dto.user.UpdateUserDTO;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.vo.user.UserDetailVO;
import com.xyehyin.hexuanning.vo.user.UserSimpleVO;
import com.xyehyin.hexuanning.vo.user.UserVO;
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