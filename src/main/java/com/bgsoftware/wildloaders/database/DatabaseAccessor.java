package com.bgsoftware.wildloaders.database;

import com.bgsoftware.wildloaders.database.sql.DBSession;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public final class DatabaseAccessor {

    public static void insertChunkLoader(BlockPosition pos, UUID placer, String loaderData, long timeLeft) {
        String sql = "REPLACE INTO {prefix}chunk_loaders(location, placer, loader_data, timeLeft) VALUES(?,?,?,?);";
        exec(sql, ps -> {
            bindLocation(ps, 1, pos);
            try {
                ps.setString(2, placer.toString());
                ps.setString(3, loaderData);
                ps.setLong(4, timeLeft);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void updateChunkLoaderTimeLeft(BlockPosition pos, long timeLeft) {
        String sql = "UPDATE {prefix}chunk_loaders SET timeLeft=? WHERE location=?;";
        exec(sql, ps -> {
            try {
                ps.setLong(1, timeLeft);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            bindLocation(ps, 2, pos);
        });
    }

    public static void deleteChunkLoader(BlockPosition pos) {
        String sql = "DELETE FROM {prefix}chunk_loaders WHERE location=?;";
        exec(sql, ps -> bindLocation(ps, 1, pos));
    }

    public static void insertNpcIdentifier(BlockPosition pos, UUID uuid) {
        String sql = "REPLACE INTO {prefix}npc_identifiers(location, uuid) VALUES(?,?);";
        exec(sql, ps -> {
            bindLocation(ps, 1, pos);
            try {
                ps.setString(2, uuid.toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void deleteNpcIdentifier(BlockPosition pos) {
        String sql = "DELETE FROM {prefix}npc_identifiers WHERE location=?;";
        exec(sql, ps -> bindLocation(ps, 1, pos));
    }

    private static void exec(String sql, Consumer<PreparedStatement> binder) {
        DBSession.waitForConnection();
        DBSession.customQuery(sql,
                new QueryResult<PreparedStatement>()
                        .onSuccess(ps -> {
                            try {
                                binder.accept(ps);
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                logFail(sql, e);
                            }
                        })
                        .onFail(e -> logFail(sql, e))
        );
    }

    private static void bindLocation(PreparedStatement ps, int idx, BlockPosition pos) {
        try {
            String v = pos == null ? "" :
                    pos.getWorld().getName() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ();
            ps.setString(idx, v);
        } catch (SQLException e) {
            logFail("bindLocation@" + idx, e);
        }
    }

    private static void logFail(String ctx, Throwable e) {
        System.out.println("[WildLoaders-SQL] Fail: " + ctx);
        e.printStackTrace();
    }
}
