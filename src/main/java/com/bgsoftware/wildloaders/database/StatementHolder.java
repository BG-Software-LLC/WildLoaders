package com.bgsoftware.wildloaders.database;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.database.sql.SQLHelper;
import com.bgsoftware.wildloaders.database.sql.session.QueryResult;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StatementHolder {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final String query;
    private final Map<Integer, Object> values = new HashMap<>();
    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private int currentIndex = 1;
    private boolean isBatch = false;

    StatementHolder(Query queryEnum) {
        this.query = queryEnum.getStatement();
    }

    public StatementHolder setString(String value) {
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setInt(int value) {
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setShort(short value) {
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setDouble(double value) {
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setBoolean(boolean value) {
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setObject(Object value) {
        values.put(currentIndex++, value);
        return this;
    }


    public StatementHolder setLocation(Location loc) {
        values.put(currentIndex++, loc == null ? "" : loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        return this;
    }

    // setLocation(BlockPosition blockPos) {
    public StatementHolder setLocation(BlockPosition blockPos) {
        values.put(currentIndex++, blockPos == null ? "" : blockPos.getWorld().getName() + "," + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ());
        return this;
    }

    public void addBatch() {
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public void prepareBatch() {
        isBatch = true;
    }

    public void execute(boolean async) {
        SQLHelper.waitForConnection();

        SQLHelper.customQuery(query, new QueryResult<PreparedStatement>()
                .onSuccess(statement -> {
                    if (isBatch) {
                        for (Map<Integer, Object> batch : batches) {
                            for (Map.Entry<Integer, Object> entry : batch.entrySet())
                                statement.setObject(entry.getKey(), entry.getValue());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    } else {
                        for (Map.Entry<Integer, Object> entry : values.entrySet())
                            statement.setObject(entry.getKey(), entry.getValue());
                        statement.executeUpdate();
                    }
                })
                .onFail(error -> {
                    WildLoadersPlugin.log("&c[SQL] Failed to execute query: " + buildReadableQuery());
                    error.printStackTrace();
                })
        );

        values.clear();
        batches.clear();
        currentIndex = 1;
    }

    private String buildReadableQuery() {
        String formattedQuery = query;
        List<Object> allValues = new ArrayList<>();

        if (isBatch && !batches.isEmpty()) {
            allValues.addAll(batches.get(0).values());
        } else {
            allValues.addAll(values.values());
        }

        for (Object value : allValues) {
            formattedQuery = formattedQuery.replaceFirst(Pattern.quote("?"), Matcher.quoteReplacement(String.valueOf(value)));
        }

        return formattedQuery;
    }
}
