package com.relax.owl.admin.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.relax.owl.admin.domain.dto.RoleCreateDTO;
import com.relax.owl.admin.domain.dto.RoleUpdateDTO;
import com.relax.owl.admin.domain.vo.RoleVO;
import com.relax.owl.admin.service.RoleAdminService;
import com.relax.owl.common.result.Result;
import com.relax.owl.common.result.ResultStatus;

import java.util.List;

/**
 * 后台角色管理：列表、详情、新增、更新、删除、启停。
 *
 * @author slnt23
 * @since 2026/8/14
 */
@RestController
@RequestMapping("/api/admin/role-do")
@RequiredArgsConstructor
@Tag(name = "后台角色管理")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleAdminController {

    private final RoleAdminService roleAdminService;
    private final com.relax.owl.user.service.AuthorizationService authorization;

    @GetMapping("/permissions")
    public Result<java.util.List<com.relax.owl.user.domain.vo.PermissionInfo>> permissions() {
        return Result.success(authorization.catalog());
    }

    @GetMapping("/{id}/permissions")
    public Result<java.util.List<String>> rolePermissions(@PathVariable Long id) {
        return Result.success(authorization.rolePermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> replacePermissions(@PathVariable Long id, @Valid @RequestBody PermissionAssignment body) {
        authorization.replacePermissions(id, body.permissions());
        return Result.success();
    }

    public record PermissionAssignment(@jakarta.validation.constraints.NotNull
            java.util.List<@jakarta.validation.constraints.NotBlank String> permissions) {}

    @GetMapping
    @Operation(summary = "角色列表")
    public Result<List<RoleVO>> list() {
        return Result.success(roleAdminService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.success(roleAdminService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增角色")
    public Result<Long> create(@Valid @RequestBody RoleCreateDTO createDTO) {
        return Result.success(roleAdminService.create(createDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<ResultStatus> update(@PathVariable Long id,
                                       @Valid @RequestBody RoleUpdateDTO updateDTO) {
        if (roleAdminService.update(id, updateDTO)) {
            return Result.success();
        }
        return Result.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<ResultStatus> delete(@PathVariable Long id) {
        if (roleAdminService.deleteById(id)) {
            return Result.success();
        }
        return Result.fail();
    }

    @PutMapping("/{id}/enabled")
    @Operation(summary = "启用/禁用角色")
    public Result<ResultStatus> updateEnabled(@PathVariable Long id,
                                              @RequestParam Boolean enabled) {
        if (roleAdminService.updateEnabled(id, enabled)) {
            return Result.success();
        }
        return Result.fail();
    }

}
