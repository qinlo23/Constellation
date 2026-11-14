package com.relax.owl.user.mapper;

import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.user.domain.entity.UserDO;
import com.relax.owl.user.domain.vo.PermissionInfo;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AuthorizationMapper {
    @Select("SELECT * FROM user_account WHERE id = #{id}")
    UserDO account(Long id);
    @Select("SELECT r.* FROM user_role r JOIN user_account_role ur ON ur.role_id=r.id WHERE ur.user_id=#{id} ORDER BY r.id")
    List<RoleDO> roles(Long id);
    @Select("SELECT DISTINCT p.code FROM user_permission p JOIN user_role_permission rp ON rp.permission_id=p.id JOIN user_role r ON r.id=rp.role_id JOIN user_account_role ur ON ur.role_id=r.id WHERE ur.user_id=#{id} AND r.enabled=1 ORDER BY p.code")
    List<String> permissions(Long id);
    @Select("SELECT code, name, module FROM user_permission ORDER BY id")
    List<PermissionInfo> catalog();
    @Select("SELECT p.code FROM user_permission p JOIN user_role_permission rp ON rp.permission_id=p.id WHERE rp.role_id=#{id} ORDER BY p.id")
    List<String> rolePermissions(Long id);
    @Insert("INSERT INTO user_account_role(user_id,role_id) VALUES(#{userId},#{roleId})")
    void assign(@Param("userId") Long userId, @Param("roleId") Long roleId);
    @Delete("DELETE FROM user_account_role WHERE user_id=#{id}")
    void clearRoles(Long id);
    @Delete("DELETE FROM user_role_permission WHERE role_id=#{id}")
    void clearPermissions(Long id);
    @Insert("INSERT INTO user_role_permission(role_id,permission_id) SELECT #{roleId},id FROM user_permission WHERE code=#{code}")
    void grant(@Param("roleId") Long roleId, @Param("code") String code);
    @Update("UPDATE user_account SET auth_version=auth_version+1 WHERE id=#{id}")
    void bumpUser(Long id);
    @Update("UPDATE user_account u JOIN user_account_role ur ON ur.user_id=u.id SET u.auth_version=u.auth_version+1 WHERE ur.role_id=#{id}")
    void bumpRole(Long id);
    // Serialize authorization changes and account removal against a stable, non-deletable row.
    @Select("SELECT id FROM user_role WHERE role_code='SUPER_ADMIN' FOR UPDATE")
    Long lockAdministration();
    @Select("SELECT COUNT(*) FROM user_account u JOIN user_account_role ur ON ur.user_id=u.id JOIN user_role r ON r.id=ur.role_id WHERE r.role_code='SUPER_ADMIN' AND u.status=0")
    long activeSuperAdmins();
}
