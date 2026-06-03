package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.entity.Permission;
import com.xyehyin.hexuanning.vo.permission.PermissionVO;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionVO toPermissionVO(Permission permission);
}