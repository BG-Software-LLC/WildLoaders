package com.bgsoftware.wildloaders.utils.database;

public enum Query {

    UPDATE_CHUNK_LOADER_TIME_LEFT("UPDATE chunk_loaders SET timeLeft=? WHERE location=?;", 2),
    INSERT_CHUNK_LOADER("REPLACE INTO chunk_loaders(location, placer, loader_data, timeLeft) VALUES(?,?,?,?);", 4),
    DELETE_CHUNK_LOADER("DELETE FROM chunk_loaders WHERE location=?;", 1),

    INSERT_NPC_IDENTIFIER("REPLACE INTO npc_identifiers(location, uuid) VALUES(?,?);", 2),
    DELETE_NPC_IDENTIFIER("DELETE FROM npc_identifiers WHERE location=?;", 1);

    private final String query;
    private final int parametersCount;

    Query(String query, int parametersCount) {
        this.query = query;
        this.parametersCount = parametersCount;
    }

    public String getStatement(){
        return query;
    }

    int getParametersCount() {
        return parametersCount;
    }

    public QueryParameters insertParameters(){
        return new QueryParameters(this);
    }

}
