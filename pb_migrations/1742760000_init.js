/// <reference path="../pb_data/types.d.ts" />

migrate((app) => {

    // ── terraces ─────────────────────────────────────────────────────────────
    const terraces = new Collection({
        name:       "terraces",
        type:       "base",
        listRule:   "",
        viewRule:   "",
        createRule: "",
        updateRule: "",
        deleteRule: "",
        fields: [
            { name: "name",            type: "text",   required: true },
            { name: "latitude",        type: "number", required: true },
            { name: "longitude",       type: "number", required: true },
            { name: "address",         type: "text" },
            { name: "sun_times",       type: "text" },
            { name: "is_covered",      type: "bool" },
            { name: "is_heated",       type: "bool" },
            { name: "size",            type: "text" },
            { name: "road_proximity",  type: "text" },
            { name: "noise_level",     type: "text" },
            { name: "view_quality",    type: "text" },
            { name: "has_vegetation",  type: "bool" },
            { name: "service_quality", type: "text" },
            { name: "price_range",     type: "text" },
            { name: "cuisine_type",    type: "text" },
            { name: "status",          type: "text" },
            { name: "device_id",       type: "text" },
            { name: "thumbs_up",       type: "number" },
            { name: "thumbs_down",     type: "number" },
        ],
    });
    app.save(terraces);

    // ── votes ─────────────────────────────────────────────────────────────────
    const terracesCollection = app.findCollectionByNameOrId("terraces");
    const votes = new Collection({
        name:       "votes",
        type:       "base",
        listRule:   "",
        viewRule:   "",
        createRule: "",
        updateRule: "",
        deleteRule: "",
        fields: [
            {
                name:          "terrace_id",
                type:          "relation",
                required:      true,
                collectionId:  terracesCollection.id,
                cascadeDelete: true,
                maxSelect:     1,
            },
            { name: "is_positive", type: "bool" },
            { name: "device_id",   type: "text" },
        ],
    });
    app.save(votes);

}, (app) => {
    app.delete(app.findCollectionByNameOrId("votes"));
    app.delete(app.findCollectionByNameOrId("terraces"));
});
