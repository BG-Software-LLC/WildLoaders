package com.bgsoftware.wildloaders.utils.database;

public enum Query {

    UPDATE_CHUNK_LOADER_TIME_LEFT("UPDATE chunk_loaders SET timeLeft=? WHERE location=?;"),
    INSERT_CHUNK_LOADER("REPLACE INTO chunk_loaders(location, placer, loader_data, timeLeft) VALUES(?,?,?,?);"),
    DELETE_CHUNK_LOADER("DELETE FROM chunk_loaders WHERE location=?;"),

    INSERT_NPC_IDENTIFIER("REPLACE INTO npc_identifiers(location, uuid) VALUES(?,?);"),
    DELETE_NPC_IDENTIFIER("DELETE FROM npc_identifiers WHERE location=?;");

    private final String query;

    Query(String query) {
        this.query = query;
    }

    public String getStatement(){
        return query;
    }

    public StatementHolder getStatementHolder(){
        return new StatementHolder(this);
    }
}
