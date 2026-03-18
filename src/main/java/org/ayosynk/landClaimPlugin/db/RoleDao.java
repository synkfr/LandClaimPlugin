package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Role;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for standalone {@link Role} persistence.
 *
 * @deprecated Roles are now stored inline within {@link org.ayosynk.landClaimPlugin.models.ClaimProfile}.
 *             This interface is retained for backward compatibility.
 */
@Deprecated
public interface RoleDao {

    /** Create or migrate role tables. */
    void createTables();

    /** Persist a role. */
    CompletableFuture<Void> saveRole(Role role);

    /** Delete a role by ID. */
    CompletableFuture<Void> deleteRole(UUID roleId);

    /** Load a single role by ID. */
    CompletableFuture<Role> getRole(UUID roleId);

    /** Load all roles. */
    CompletableFuture<List<Role>> getAllRoles();
}
