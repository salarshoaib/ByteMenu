package com.bytemenu.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Sprint 3 — tracks which orders were last status-set by the Admin/Chef Portal.
 * Any order ID in this set is considered chef-locked: student-side auto-advance
 * will skip it entirely.  Only the chef (via AdminController) can add or remove IDs.
 */
public class ChefVetoManager {

    private static final Set<Integer> lockedIds =
            Collections.synchronizedSet(new HashSet<>());

    /** Called by AdminController when the chef clicks a status button. */
    public static void lock(int orderId) {
        lockedIds.add(orderId);
    }

    /** Returns true if the chef has ever manually set this order's status. */
    public static boolean isLocked(int orderId) {
        return lockedIds.contains(orderId);
    }

    /** Bulk-seed when the portal first loads (so existing orders stay locked). */
    public static void lockAll(java.util.Collection<Integer> ids) {
        lockedIds.addAll(ids);
    }
}
