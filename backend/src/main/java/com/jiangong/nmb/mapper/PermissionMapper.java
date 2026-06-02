package com.jiangong.nmb.mapper;

import com.jiangong.nmb.entity.Permission;
import com.jiangong.nmb.vo.permission.PermissionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionVO toPermissionVO(Permission permission);
}