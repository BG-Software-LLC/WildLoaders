package com.bgsoftware.wildloaders.utils.database;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class QueryParameters {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final Query query;
    private final List<Object> parameters;

    public QueryParameters(Query query){
        this.query = query;
        this.parameters = new ArrayList<>(query.getParametersCount());
    }

    public Query getQuery() {
        return query;
    }

    public void executeQuery(PreparedStatement preparedStatement) throws SQLException {
        for(int i = 0; i < parameters.size(); i++)
            preparedStatement.setObject(i + 1, parameters.get(i));
    }

    public void queue(Object caller){
        DatabaseQueue.queue(caller, this);
    }

    public QueryParameters setLocation(Location loc){
        return setObject(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
    }

    public QueryParameters setObject(Object object){
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
