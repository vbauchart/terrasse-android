migrate((app) => {
    const votes = app.findCollectionByNameOrId("votes");

    // Supprimer les doublons éventuels (garder le plus récent par terrace_id + device_id)
    // avant d'ajouter la contrainte unique
    app.db().newQuery(`
        DELETE FROM votes
        WHERE id NOT IN (
            SELECT id FROM votes
            GROUP BY terrace_id, device_id
            HAVING id = MAX(id)
        )
    `).execute();

    // Ajouter l'index unique
    votes.indexes = (votes.indexes || []).concat([
        "CREATE UNIQUE INDEX idx_votes_terrace_device ON votes (terrace_id, device_id)"
    ]);
    app.save(votes);
}, (app) => {
    const votes = app.findCollectionByNameOrId("votes");
    votes.indexes = (votes.indexes || []).filter(
        (idx) => !idx.includes("idx_votes_terrace_device")
    );
    app.save(votes);
});
