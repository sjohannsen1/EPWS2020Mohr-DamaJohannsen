const knex = require("./knex.js");

function getMittelpunkt(laendercode) {
    return knex("mittelpunkt").where("laendercode", laendercode).select();
}

function getGeojson(laendercode) {
    return knex("geo_daten").where("laendercode", laendercode).select();
}

function getProduce(name) {
    return knex("produce").where("name", name).select();
}

module.exports = {
    getMittelpunkt,
    getGeojson,
    getProduce
};