migrate((app) => {
    // Collection auth anonyme pour les appareils
    const devices = new Collection({
        name: "devices",
        type: "auth",
        createRule: "",       // enregistrement ouvert (auto-registration)
        listRule: null,       // liste non accessible
        viewRule: "@request.auth.id = id",
        updateRule: "@request.auth.id = id",
        deleteRule: null,
    });
    app.save(devices);

    // Restreindre terrasses aux appareils authentifiés
    const terraces = app.findCollectionByNameOrId("terraces");
    terraces.listRule   = "@request.auth.id != ''";
    terraces.viewRule   = "@request.auth.id != ''";
    terraces.createRule = "@request.auth.id != ''";
    terraces.updateRule = "@request.auth.id != ''";
    terraces.deleteRule = "@request.auth.id != ''";
    app.save(terraces);

    // Restreindre votes aux appareils authentifiés
    const votes = app.findCollectionByNameOrId("votes");
    votes.listRule   = "@request.auth.id != ''";
    votes.viewRule   = "@request.auth.id != ''";
    votes.createRule = "@request.auth.id != ''";
    votes.updateRule = "@request.auth.id != ''";
    votes.deleteRule = "@request.auth.id != ''";
    app.save(votes);

}, (app) => {
    const terraces = app.findCollectionByNameOrId("terraces");
    terraces.listRule = ""; terraces.viewRule = ""; terraces.createRule = "";
    terraces.updateRule = ""; terraces.deleteRule = "";
    app.save(terraces);

    const votes = app.findCollectionByNameOrId("votes");
    votes.listRule = ""; votes.viewRule = ""; votes.createRule = "";
    votes.updateRule = ""; votes.deleteRule = "";
    app.save(votes);

    try { app.delete(app.findCollectionByNameOrId("devices")); } catch(e) {}
});
