const knex = require("knex");

const connectedKnex = knex({
    client: "sqlite3",
    connection: {
        filename: "Datenbank.db3"
    }

});

module.exports = connectedKnex;