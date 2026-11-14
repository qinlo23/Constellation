#!/usr/bin/env python3
"""Exercise RBAC SQL against a disposable MySQL 8.4 container, never a project DB.

Start a temporary mysql:8.4.9 container named constellation-rbac-test-* with
MYSQL_ROOT_PASSWORD=rbac-local-test, then pass its name as the sole argument.
The Java persistence suite can subsequently use the constellation_rbac_java schema.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
CONTAINER = sys.argv[1] if len(sys.argv) == 2 else ""
if not CONTAINER.startswith("constellation-rbac-test-"):
    raise SystemExit("Pass the name of a disposable constellation-rbac-test-* MySQL container")


def mysql(sql, schema=None, success=True):
    command = ["docker", "exec", "-i", "--env", "MYSQL_PWD=rbac-local-test", CONTAINER,
               "mysql", "--batch", "--skip-column-names", "-uroot"]
    if schema:
        command.append(schema)
    result = subprocess.run(command, input=sql, text=True, capture_output=True)
    if success and result.returncode:
        raise AssertionError(result.stderr)
    if not success and result.returncode == 0:
        raise AssertionError("Expected SQL to reject invalid data")
    return result.stdout.strip()


def reset(schema, legacy=False):
    assert schema.startswith("constellation_rbac_")
    mysql(f"DROP DATABASE IF EXISTS {schema}; CREATE DATABASE {schema} CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;")
    directory = ROOT / "database" / ("tests/fixtures" if legacy else "modules")
    prefix = "legacy_" if legacy else ""
    mysql((directory / f"{prefix}user_db.sql").read_text(), schema)
    mysql((directory / f"{prefix}user_init_data.sql").read_text(), schema)


for filename in ["user_db.sql", "user_init_data.sql", "user_permission_seed.sql"]:
    assert (ROOT / "database/modules" / filename).read_bytes() == (ROOT / "docker/mysql-owl/owl/user" / filename).read_bytes()
permission_seed = (ROOT / "database/modules/user_permission_seed.sql").read_text().split("\n", 1)[1]
assert permission_seed in (ROOT / "database/modules/user_init_data.sql").read_text()
assert permission_seed in (ROOT / "database/migrations/20261114_user_rbac.sql").read_text()

migration = (ROOT / "database/migrations/20261114_user_rbac.sql").read_text()
reset("constellation_rbac_fresh")
assert mysql("SELECT COUNT(*) FROM user_account_role", "constellation_rbac_fresh") == "4"
assert mysql("SELECT role_code FROM user_role WHERE role_name='ADMIN'", "constellation_rbac_fresh") == "SUPER_ADMIN"
assert mysql("SELECT COUNT(*) FROM user_permission", "constellation_rbac_fresh") == "28"
mysql((ROOT / "database/modules/user_init_data.sql").read_text(), "constellation_rbac_fresh")
assert mysql("SELECT COUNT(*) FROM user_account_role", "constellation_rbac_fresh") == "4"
print("PASS fresh install, default memberships and idempotent seed")

reset("constellation_rbac_migrated", legacy=True)
mysql(migration, "constellation_rbac_migrated")
assert mysql("SELECT COUNT(*) FROM user_account_role", "constellation_rbac_migrated") == "4"
assert mysql("SELECT role_code FROM user_role WHERE role_name='ADMIN'", "constellation_rbac_migrated") == "SUPER_ADMIN"
assert mysql("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_account' AND column_name='role_name'", "constellation_rbac_migrated") == "0"
assert mysql("SELECT COUNT(*) FROM user_role_permission", "constellation_rbac_migrated") == "0"
assert mysql("SELECT COUNT(*) FROM user_permission", "constellation_rbac_migrated") == "28"
print("PASS existing accounts and custom roles migrate without accidental grants")

reset("constellation_rbac_orphan", legacy=True)
mysql("SET FOREIGN_KEY_CHECKS=0; UPDATE user_account SET role_name='missing' WHERE username='user';", "constellation_rbac_orphan")
mysql(migration, "constellation_rbac_orphan", success=False)
assert mysql("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND column_name='auth_version'", "constellation_rbac_orphan") == "0"
print("PASS orphan preflight rejects before schema mutation")

reset("constellation_rbac_noadmin", legacy=True)
mysql("UPDATE user_account SET status=1 WHERE role_name='ADMIN'", "constellation_rbac_noadmin")
mysql(migration, "constellation_rbac_noadmin", success=False)
assert mysql("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND column_name='auth_version'", "constellation_rbac_noadmin") == "0"
print("PASS missing active administrator rejects before schema mutation")

reset("constellation_rbac_java")
print("Prepared constellation_rbac_java for the optional Java persistence/concurrency suite")
