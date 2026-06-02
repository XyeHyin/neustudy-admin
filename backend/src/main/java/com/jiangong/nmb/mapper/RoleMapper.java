package com.jiangong.nmb.mapper;

import com.jiangong.nmb.dto.role.CreateRoleDTO;
import com.jiangong.nmb.dto.role.UpdateRoleDTO;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.vo.role.RoleDetailVO;
import com.jiangong.nmb.vo.role.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleVO toRoleVO(Role role);

    RoleDetailVO toRoleDetailVO(Role role);

    Role toRole(CreateRoleDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateRoleFromDto(UpdateRoleDTO dto, @MappingTarget Role role);
}