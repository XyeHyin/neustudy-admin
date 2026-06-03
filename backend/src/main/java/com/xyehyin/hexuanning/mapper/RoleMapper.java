package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.role.CreateRoleDTO;
import com.xyehyin.hexuanning.dto.role.UpdateRoleDTO;
import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.vo.role.RoleDetailVO;
import com.xyehyin.hexuanning.vo.role.RoleVO;
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