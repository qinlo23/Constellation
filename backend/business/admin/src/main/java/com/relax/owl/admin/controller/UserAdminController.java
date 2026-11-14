package com.relax.owl.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.relax.owl.admin.domain.dto.UserCreateDTO;
import com.relax.owl.admin.domain.dto.UserPasswordResetDTO;
import com.relax.owl.admin.domain.dto.UserUpdateDTO;
import com.relax.owl.admin.domain.vo.AdminUserVO;
import com.relax.owl.admin.service.UserAdminService;
import com.relax.owl.common.result.Result;
import com.relax.owl.common.result.ResultPage;
import com.relax.owl.common.result.ResultStatus;

/**
 * 后台用户管理：分页/搜索、新增、修改、删除、封禁、角色变更、重置密码。
 *
 * @author slnt23
 * @since 2026/8/14
 */
@RestController
@RequestMapping("/api/admin/user-do")
@RequiredArgsConstructor
@Tag(name = "后台用户管理")
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * 分页获取用户信息，支持关键字搜索和状态/角色筛选。
     */
    @GetMapping
    @Operation(summary = "用户分页列表")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:read')")
    public Result<ResultPage<AdminUserVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Long roleId) {
        return Result.success(userAdminService.page(pageNum, pageSize, keyword, status, roleId));
    }

    /**
     * 获取用户详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:read')")
    public Result<AdminUserVO> getById(@PathVariable Long id) {
        return Result.success(userAdminService.getById(id));
    }

    /**
     * 新增用户。
     */
    @PostMapping
    @Operation(summary = "新增用户")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:create')")
    public Result<Long> create(@Valid @RequestBody UserCreateDTO createDTO) {
        return Result.success(userAdminService.create(createDTO));
    }

    /**
     * 更新用户基础资料、状态或角色。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:update')")
    public Result<ResultStatus> update(@PathVariable Long id,
                                       @Valid @RequestBody UserUpdateDTO updateDTO) {
        if (userAdminService.update(id, updateDTO)) {
            return Result.success();
        }
        return Result.fail();
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:delete')")
    public Result<ResultStatus> delete(@PathVariable Long id) {
        if (userAdminService.deleteById(id)) {
            return Result.success();
        }
        return Result.fail();
    }

    /**
     * 封禁/解封用户，status：0=正常，1=封禁。
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "封禁/解封用户")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:status')")
    public Result<ResultStatus> updateStatus(@PathVariable Long id,
                                             @RequestParam Byte status) {
        if (userAdminService.updateStatus(id, status)) {
            return Result.success();
        }
        return Result.fail();
    }

    @PutMapping("/{id}/roles")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "替换用户角色，成功后需要重新登录")
    public Result<Void> updateRoles(@PathVariable Long id, @Valid @RequestBody RoleAssignment body) {
        userAdminService.updateRoles(id, body.roleIds());
        return Result.success();
    }

    public record RoleAssignment(@jakarta.validation.constraints.NotEmpty
            java.util.List<@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long> roleIds) {}

    /**
     * 重置用户密码，成功后旧 token 全部失效。
     */
    @PutMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('user:reset-password')")
    public Result<ResultStatus> resetPassword(@PathVariable Long id,
                                              @Valid @RequestBody UserPasswordResetDTO resetDTO) {
        if (userAdminService.resetPassword(id, resetDTO)) {
            return Result.success();
        }
        return Result.fail();
    }

}
