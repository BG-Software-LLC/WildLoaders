package com.bgsoftware.wildloaders.utils.database;

import com.bgsoftware.wildloaders.utils.BlockPosition;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class QueryParameters {

    private final Query query;
    private final List<Object> parameters;

    public QueryParameters(Query query) {
        this.query = query;
        this.parameters = new ArrayList<>(query.getParametersCount());
    }

    public Query getQuery() {
        return query;
    }

    public void executeQuery(PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < parameters.size(); i++)
            preparedStatement.setObject(i + 1, parameters.get(i));
    }

    public void queue(Object caller) {
        DatabaseQueue.queue(caller, this);
    }

    public QueryParameters setLocation(Location loc) {
        return setObject(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
    }

    public QueryParameters setLocation(BlockPosition blockPos) {
        return setObject(blockPos.getWorldName() + "," + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ());
    }

    public QueryParameters setObject(Object object) {
        if (object instanceof Location)
            return setLocation((Location) object);

        parameters.add(object);
        return this;
    }

    @Override
    public String toString() {
        return "QueryParameters{" +
                "query=" + query +
                ", parameters=" + parameters +
                '}';
    }

}
