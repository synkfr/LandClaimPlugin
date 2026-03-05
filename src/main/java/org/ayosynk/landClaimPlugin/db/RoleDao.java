package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Role;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RoleDao {
    void createTables();

    CompletableFuture<Void> saveRole(Role role);

    CompletableFuture<Void> deleteRole(UUID roleId);

    CompletableFuture<Role> getRole(UUID roleId);

    CompletableFuture<List<Role>> getAllRoles();
}
